# Nghiên cứu Java AST và phương án tích hợp với apps/pcm-webapp

## 1. Bối cảnh & yêu cầu

- AI Assistant trong `apps/pcm-webapp` hiện chưa có khả năng hiểu sâu cấu trúc Java do thiếu lớp biểu diễn cú pháp (AST)
  và metadata liên quan.
- Việc quét mã nguồn trực tiếp từ môi trường trình duyệt (hoặc từ chính `apps/pcm-webapp`) để sinh AST là **bất khả thi
  ** vì rủi ro bảo mật, giới hạn tài nguyên và độ trễ khi phải đọc toàn bộ codebase lớn.
- Mục tiêu: xây dựng quy trình ngoại vi (out-of-band) để sinh AST Java với độ chính xác cao nhất, sau đó phục vụ lại
  cho `apps/pcm-webapp` thông qua API/knowledge base để LLM có thể truy hồi ngữ cảnh code chất lượng cao.

## 2. Tổng quan về AST Java

- **AST (Abstract Syntax Tree)** mô tả cấu trúc cú pháp của mã nguồn sau khi loại bỏ chi tiết không cần thiết (dấu
  ngoặc, layout), cho phép phân tích quan hệ giữa packages, classes, methods, fields, control flows...
- Vì Java là ngôn ngữ tĩnh, độ chính xác của AST phụ thuộc cực mạnh vào việc **giải quyết symbol/type** (classpath,
  annotation processors, generated sources).
- Một AST tốt cho LLM nên kèm theo:
    - Qualified name đầy đủ và toạ độ file/line.
    - Thông tin phụ trợ (Javadoc, modifiers, dependency edges, call graph cơ bản).
    - Phiên bản hóa (commit hash, build id) để bảo đảm truy hồi đúng nguồn.

## 3. Đánh giá công cụ/SDK sinh AST

| Công cụ                        | Ưu điểm                                                                                                         | Hạn chế                                                                               | Mức phù hợp                                                   |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|---------------------------------------------------------------|
| **Eclipse JDT Core ASTParser** | Parser cấp compiler, hỗ trợ incremental build, hiểu Java 21, xử lý annotation processors thông qua Eclipse APT. | Phải dựng đầy đủ classpath + project settings; API phức tạp.                          | ★★★★☆ (Chính xác nhất khi cần symbol resolution).             |
| **Spoon (Inria)**              | Cung cấp model giàu thông tin (CtModel), kèm query DSL, dễ serialize sang JSON.                                 | Chi phí khởi tạo lớn; cần Gradle/Maven launcher để resolve classpath.                 | ★★★★☆ (Tốt khi cần thao tác sâu trên model).                  |
| **JavaParser + SymbolSolver**  | Nhẹ, dễ embed vào CLI, hỗ trợ parsing độc lập từng file.                                                        | Độ chính xác phụ thuộc cấu hình TypeSolver; khó bao phủ các project modular phức tạp. | ★★★☆☆ (Phù hợp quick scan, nhưng thiếu đảm bảo tuyệt đối).    |
| **tree-sitter-java**           | Rất nhanh, incremental, dễ chạy ở edge.                                                                         | Không hiểu type; output cần chuyển đổi thêm; không xử lý macro build.                 | ★★☆☆☆ (Hữu dụng khi cần tốc độ, không phải độ chính xác cao). |

👉 **Kết luận:** Để đạt độ chính xác cao nhất, pipeline nên ưu tiên **JDT** hoặc **Spoon** chạy cùng cấu hình build chính
thức (Gradle/Maven). Tree-sitter chỉ nên dùng cho highlight/preview.

## 4. Kiến trúc giải pháp (ngoài pcm-webapp)

```
┌────────────┐     ┌─────────────────────────┐     ┌────────────────┐
│ Git Source │ ──▶ │ AST Build Service (CI)  │ ──▶ │ AST + KB Store │
└────────────┘     │ - Gradle/Maven sync     │     │ - Object store │
                   │ - JDT/Spoon parse       │     │ - Vector index │
                   │ - Metadata normalizer   │     └────────┬───────┘
                   └─────────────────────────┘              │
                                                            ▼
                                               ┌────────────────────────┐
                                               │ apps/pcm-webapp (LLM)  │
                                               │ - Function calls        │
                                               │ - Retrieval adapters    │
                                               └────────────────────────┘
```

### 4.1 Thành phần chính

1. **AST Build Service (chạy trong CI/CD hoặc worker riêng)**
    - Lấy code từ GitHub/monorepo theo commit/tag.
    - Chạy `./gradlew classes` hoặc `mvn -q -DskipTests compile` để chắc chắn classpath chính xác.
    - Thực thi module collector:
      ```bash
      java -jar ast-builder.jar \
        --root /workspace/project \
        --tool jdt \
        --classpath $(./gradlew printClasspath) \
        --out /tmp/ast-bundle.jsonl
      ```
    - Chuẩn hóa output: mỗi node gồm `symbolId`, `kind`, `signature`, `doc`, `relations` (extends, implements, calls,
      uses), `span`.

2. **AST + Knowledge Base Store**
    - Lưu AST thô (JSONL/parquet) kèm chỉ mục `symbolId -> file`.
    - Sinh thêm level tóm tắt: `method_summary`, `class_overview`, `dependency_graph`.
    - Embed các đoạn quan trọng (method body, docstring, call graph) vào vector DB (PGVector, Qdrant).
    - Gắn metadata `projectId`, `commitSha`, `buildTime` để phục vụ truy hồi phiên bản.

3. **Service API layer**
    - REST/gRPC nhỏ để `apps/pcm-webapp` gọi, ví dụ:
        - `POST /ast/search` → tìm symbol theo tên/ký hiệu.
        - `GET /ast/{symbolId}` → trả AST subtree + code span.
        - `POST /ast/retrieve` → semantic search bằng embedding.
    - Tất cả chạy server-side, không yêu cầu trình duyệt tải toàn bộ repo.

## 5. Tích hợp vào apps/pcm-webapp

| Bước                               | Mô tả                                                                                                                            | Note                                                                      |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| 1. Tạo **ASTProvider** mới         | Thêm adapter trong lớp AI tools để gọi API `ast/search`, `ast/get_context`.                                                      | Syntax tương tự các provider hiện tại (`GitHub Tools`, `Database Tools`). |
| 2. Mở rộng function calling schema | Định nghĩa function `get_java_symbol_context`, `search_java_usages`.                                                             | Input gồm `projectId`, `symbol`, `filters`.                               |
| 3. Quy trình truy vấn              | Khi user hỏi về code Java, LLM lập kế hoạch → gọi `get_java_symbol_context` → backend fetch AST subtree + snippet → LLM trả lời. | Không cần đọc file trực tiếp trong webapp.                                |
| 4. Đồng bộ phiên bản               | PCM webapp gửi `projectId + gitSha` để service chọn đúng snapshot AST.                                                           | Ngăn LLM dùng dữ liệu lỗi thời.                                           |
| 5. Giám sát chất lượng             | Log event `ast_hit_rate`, `retrieval_latency`, `mismatch_detected`.                                                              | Cho phép rollback nếu AST sai.                                            |

## 6. Chiến lược đảm bảo độ chính xác cao

- **Bám sát build chính thức:** luôn chạy parse sau khi build thành công để bảo đảm annotation processors (Lombok,
  MapStruct, AutoValue...) đã tạo source cần thiết.
- **Type resolution bắt buộc:** JDT/Spoon phải được cung cấp đầy đủ `--classpath` gồm cả compiled jars của modules phụ
  thuộc.
- **Validation tự động:**
    - So sánh số class/method trong AST với kết quả từ `javap -classpath ...` hoặc `ctModel.getElements`.
    - Chạy test snapshot (ví dụ: parse một module chuẩn, đối chiếu checksum AST).
    - Publish metrics (số node parse lỗi, thời gian parse, dung lượng output).
- **Incremental update:** dùng Git diff để chỉ parse lại modules thay đổi, giảm thời gian và đảm bảo dữ liệu mới luôn
  có.
- **Phiên bản hoá dữ liệu:** luồng publish phải ghi `ast_version.json`
  chứa `commitSha`, `toolVersion`, `parserOptions` → giúp PCM webapp báo lỗi khi hỏi dữ liệu cũ.

## 7. Định hướng mở rộng đa ngôn ngữ

- **JSP**: kết hợp AST Java (cho scriptlet) và parser HTML/XML (jsoup, TagSoup) để bảo toàn layout, gắn từng node JSP
  với `symbolId` Java tương ứng nhằm truy dấu logic server-side.
- **HTML**: dùng parse5 hoặc tree-sitter-html để sinh DOM tree chuẩn hóa, bổ sung metadata accessibility/layout giúp LLM
  hiểu cấu trúc UI khi mapping với backend.
- **JavaScript/TypeScript**: khai thác `tsserver`, Babel hoặc SWC để xuất AST chuẩn ESTree, kèm source map về file
  bundle để giữ liên hệ giữa modules và artifacts build.
- **XFDL**: áp parser XML (SAX/DOM) rồi ánh xạ sang schema chung (form, field, binding expression, validation) để LLM dễ
  truy vấn cấu hình biểu mẫu.
- **Hạ tầng chung**: mỗi ngôn ngữ có `ast-builder` riêng nhưng dùng chung pipeline `Build Service → Store → API`.
  Storage cần metadata `language`, `framework` để PCM webapp có thể hợp nhất hoặc lọc ngữ cảnh đa ngôn ngữ trong câu trả
  lời.

## 8. Lộ trình triển khai đề xuất

1. **Tuần 1-2:** xây dựng PoC AST builder (CLI) dựa trên JDT + Gradle, tạo output JSONL + metrics.
2. **Tuần 3:** dựng kho lưu trữ (S3 + Postgres) và script publish từ CI (GitHub Actions/Turborepo pipeline).
3. **Tuần 4:** tạo service API nhẹ (NestJS/FastAPI) + auth key cho PCM webapp.
4. **Tuần 5:** thêm `ASTProvider` + function definitions vào `apps/pcm-webapp` (docs, types, unit test).
5. **Tuần 6:** thử nghiệm end-to-end, đo chất lượng câu trả lời của LLM, bổ sung guardrails (fallback sang GitHub raw
   file nếu thiếu AST).

## 9. Rủi ro & biện pháp

- **Classpath thiếu** → AST mất symbol → Thiết lập kiểm tra build, bắt buộc `compileJava` hoàn tất trước khi parse.
- **Dữ liệu quá lớn** → khó tải vào LLM → Áp dụng chunking theo symbol và embedding retrieval thay vì đưa toàn bộ AST.
- **Độ trễ API** → cache layer trong PCM webapp cho các symbol truy cập thường xuyên.
- **Bảo mật mã nguồn** → AST builder chạy trong môi trường CI nội bộ, kết quả mã hoá khi lưu trữ, API yêu cầu token ngắn
  hạn.

---

Tài liệu này cung cấp cơ sở nghiên cứu và kiến trúc đề xuất để LLM hiểu code Java với độ chính xác cao mà không cần
cho `apps/pcm-webapp` trực tiếp quét file. Triển khai theo các bước trên sẽ giúp mở rộng AI Assistant thành một công cụ
phân tích code chuyên sâu, linh hoạt cho các dự án Java lớn.

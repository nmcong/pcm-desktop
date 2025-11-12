# Báo cáo: Liên kết PCM với Source Code

## 1. Bài toán

- AI Chat trong `apps/pcm-webapp` hiện chỉ biết dữ liệu business (subsystem, project, screen, events...).
- Người dùng muốn hỏi: “Sửa logic phê duyệt ở Finance Approval Desk cần chạm file nào?”, “Field này map sang bảng DB nào?”, “Tôi cần đổi API nào?” → cần liên kết dữ liệu PCM với codebase thực tế.
- Không có backend nên toàn bộ indexing/phân tích phải chạy offline (CLI/desktop tool), sau đó import vào PCM để LLM query.

## 2. Yêu cầu chức năng

1. **Từ PCM → Source**: mỗi entity (subsystem/project/screen/event) phải có danh sách file, component, database table, API tương ứng.
2. **Từ Source → PCM**: khi người dùng chọn file trong IDE, có thể truy ra screen hoặc workflow nào sử dụng.
3. **AI Chat**: có thể gọi tool để trả lời “file nào liên quan”, “đoạn AST nào chứa hàm x”, “bảng DB bị ảnh hưởng”.

## 3. Đề xuất kiến trúc

### 3.1 Desktop/CLI Indexer

- Công cụ chạy ngoài (Node CLI hoặc Electron mini) với các bước:
  1. **Quét repo**: parse ts/js/tsx/json/sql ... tạo AST (có thể dùng `ts-morph` cho TypeScript, `babel` cho JS, `tree-sitter` nếu cần đa ngôn ngữ).
  2. **Trích metadata**:
     - Component name, props, hook custom.
     - API call (fetch/axios path, method).
     - Database query (SQL file, ORM call).
     - Regex map `SCREEN_ID`/`PROJECT_ID` → file bằng comment `@pcm-screen 104`.
  3. **Nén dữ liệu** thành JSON/SQLite nhẹ. Ví dụ:
     ```json
     {
       "screens": {
         "104": {
           "sourceFiles": ["apps/refund-intake/src/pages/FinanceApproval.tsx"],
           "components": ["FinanceApprovalForm", "PayoutScheduleModal"],
           "apiCalls": [
             { "method": "POST", "path": "/payout/v2/schedule", "file": "FinanceApproval.tsx", "line": 120 }
           ],
           "dbTables": ["refund_cases", "payout_batches"]
         }
       },
       "files": {
         "apps/refund-intake/src/pages/FinanceApproval.tsx": {
           "screens": ["104"],
           "exports": ["FinanceApprovalPage"],
           "apiCalls": [...],
           "astSummary": "React component + hook useQuery(payout_batches)"
         }
       }
     }
     ```
  4. **Xuất file**: `pcm-code-index.json`.

- Tool có thể chạy theo lệnh:
  ```bash
  pnpm pcm:index-code --src ../noteflix --out apps/pcm-webapp/public/assets/code-index.json
  ```

### 3.2 Import vào PCM

- Khi khởi động PCM, load `code-index.json` (IndexedDB hoặc fetch static).
- Mỗi project/screen trong sample JSON có thể chỉ lưu `codeRefs: [fileId]`, khi render UI hiển thị “Code references” tab.
- AI Chat tool definitions mở rộng:
  - `find_source_by_screen(screenId)` → trả danh sách file, component, API.
  - `search_code_by_keyword(keyword)` → fuzzy search trong metadata.
  - `get_db_usage(tableName)` → liệt kê screens/events dùng bảng đó.

## 4. Luồng AI Chat gợi ý

1. User chat: “Quy trình payout gọi API nào và file nào handle?”.
2. System prompt phát hiện keyword `payout`.
3. Tool pipeline:
   - `search_projects` / `search_screens` → ra screen 104 (Finance Approval).
   - `find_source_by_screen(104)` (tool mới, đọc từ `code-index.json`).
4. LLM trả lời:
   ```
   - UI: apps/refund-intake/src/pages/FinanceApproval.tsx (component FinanceApprovalPage)
   - API: POST /payout/v2/schedule (được gọi tại dòng 118 trong file trên)
   - DB: bảng payout_batches, refund_cases
   ```
5. Nếu user tiếp “Tôi muốn biết phần audit log ở đâu?” → tool `search_code_by_keyword("audit log")` + `get_screen_details` → linking ready.

## 5. Lựa chọn công nghệ AST

| Công cụ                        | Ưu điểm                        | Nhược                            |
| ------------------------------ | ------------------------------ | -------------------------------- |
| `ts-morph` (TypeScript AST)    | API thân thiện, hiểu TS/JS/TSX | Nặng RAM nếu scan cả repo lớn    |
| `tree-sitter`                  | Đa ngôn ngữ (TS, SQL...)       | Cần binding, phức tạp            |
| Regex nhẹ + comment annotation | Nhanh, ít phụ thuộc            | Thiếu chính xác, khó giữ đồng bộ |

Khuyến nghị: bắt đầu với `ts-morph` + `sqlparser` đơn giản; khi cần đa ngôn ngữ mới chuyển sang tree-sitter.

## 6. Quy trình triển khai đề xuất

1. **Chuẩn hoá annotation**: trong code, thêm comment `// @pcm-screen:104` hoặc `// @pcm-project:RIP` để indexer map chuẩn xác.
2. **Xây CLI**:
   - Input: path repo, config file (regex, alias).
   - Output: JSON + checksum.
   - Hỗ trợ incremental (dựa trên git diff).
3. **Tích hợp PCM**:
   - Thêm UI hiển thị code reference trong Project/Screen detail.
   - Mở rộng DatabaseQueryTool với các tool mới đọc `code-index`.
4. **AI Prompt**:
   - Cập nhật system prompt: “Khi người dùng hỏi về code/database, hãy gọi các tool `find_source_by_*`, `list_tables_by_*` để lấy thông tin chính xác thay vì đoán.”
   - LLM trả lời phải dẫn link file + line (nếu có).

## 7. Lộ trình dài hạn

- **Desktop companion app**: cung cấp UI đơn giản để chọn repo, cấu hình mapping, bấm “Generate code index”, sau đó tự copy file vào PCM.
- **Automation**: trong CI, chạy indexer khi merge vào main để đảm bảo dữ liệu luôn mới.
- **Expansion**: index test cases, API schema, infra config → AI có thể trả lời “cần cập nhật secret nào”, “kịch bản test nằm ở đâu”, v.v.

## 8. Kết luận

- Giải pháp khả thi nhất hiện tại: xây một tool client-side (CLI/Electron) quét source, xuất JSON metadata (dựa trên AST/regex).
- PCM import file này và cung cấp tool API cho LLM → AI Chat có thể trả lời câu hỏi liên quan source code/database chính xác mà không cần backend.
- Cần thiết lập quy chuẩn annotation + cập nhật sample JSON để gắn `codeRefs` giúp trải nghiệm đồng bộ giữa dữ liệu nghiệp vụ và codebase.

# AI Panel & Components Review

Ngày review: `2025-02-14`  
Phạm vi: `apps/pcm-webapp/public/js/components/AIPanel.js` và thư mục `components/ai/*`

## 1. Đồng bộ history với Conversation Manager

- **Vấn đề**: Khi người dùng mở `Chat History` và load lại một cuộc hội thoại (`AIHistoryModal.js:230-254`), panel chỉ gán `aiPanel.currentConversationId` nhưng không cập nhật `AIConversationManager.currentConversationId`. Sau đó `conversationManager.getCurrentConversationId()` vẫn trả về ID cũ (hoặc `null`) nên lần gửi tiếp theo sẽ tạo conversation “New Conversation” mới => lịch sử hiển thị và dữ liệu lưu trữ bị lệch.
- **Hướng xử lý**:
  1. Cung cấp thêm method `setCurrentConversation(id)` trong `AIConversationManager`.
  2. Gọi method này ngay sau khi người dùng chọn một entry trong `AIHistoryModal.loadConversation`.
  3. Đảm bảo `startNewConversation()` cũng reset cả panel lẫn manager để trạng thái đồng bộ tuyệt đối.

## 2. Thiếu lưu trữ message khi stream

- **Vấn đề**: `AIPanel.handleFunctionCallingMode` và `handleContextInjectionMode` dựng context bằng `this.chatView.getMessages()` (AIPanel.js:372, 518). Tuy nhiên `AIChatView.addMessage` chỉ được gọi với user message và các provider không streaming; khi provider hỗ trợ stream thì `AIStreamingHandler` render trực tiếp mà không push nội dung cuối cùng vào `chatView.messages`. Kết quả: vòng lặp kế tiếp không có assistant turn, làm LLM quên ngữ cảnh, tool-calling dễ rơi vào loop sai.
- **Hướng xử lý**:
  1. Sau khi stream kết thúc (`AIStreamingHandler._streamInternal`), gọi hook để ghi `{ role: "assistant", content: fullContent }` vào `AIChatView.messages`.
  2. Hoặc đơn giản hơn: thay vì lấy từ `chatView`, sử dụng dữ liệu đã được lưu trong `conversationManager`/IndexedDB làm nguồn sự thật khi build `conversationMessages`.

## 3. Feature toggles không được load lại

- **Vấn đề**: `AISettingsModal.saveSettings` lưu `ai-database-access` và `ai-function-calling` vào `localStorage`, nhưng `AIPanel` chỉ đọc `ai-conversation-settings`. Nghĩa là mỗi lần reload trang, cả Database Access lẫn Function Calling đều auto bật (`true` ở constructor, dòng 42-45) bất kể người dùng đã tắt trước đó.
- **Hướng xử lý**:
  1. Trong `AIPanel.init()` (hoặc constructor), đọc hai khoá trên từ `localStorage` và cập nhật `this.enableDatabaseAccess`, `this.enableFunctionCalling`.
  2. Khi settings thay đổi, phản ánh ngay lên badge UI (ví dụ hiển thị tooltip nếu DB access đang off để tránh kỳ vọng sai).

## 4. Nguy cơ XSS từ Markdown

- **Vấn đề**: `MarkdownRenderer.initializeMarked()` đặt `sanitize: false` và các renderer (`code`, `link`, `table`, …) đều trả về string inject thẳng `innerHTML`. Nội dung assistant đến từ LLM nên nếu bị prompt-injection có thể chứa `<img onerror>`/`<script>`/`<iframe>` và chạy trong PCM UI.
- **Hướng xử lý**:
  1. Tích hợp thư viện như [DOMPurify](https://github.com/cure53/DOMPurify) để sanitize kết quả `marked` trước khi set vào DOM.
  2. Nếu chưa thể thêm dependency, bật `sanitize: true` và tự định nghĩa allowlist tối thiểu (code, link, table) nhưng vẫn loại bỏ event handler/inline script.
  3. Khi render link, bổ sung `rel="noopener noreferrer"` đã có sẵn, nhưng cần đảm bảo `href` được validate (chỉ cho phép http(s)/mailto) để tránh `javascript:` URL.

## 5. Đề xuất bổ sung

- Gộp log tool executor (`ToolExecutor.js`) về mức debug hoặc guard bằng flag để tránh spam console trên production.
- Tách logic merge context (database injection) ra `ConversationBuilder` chung để giảm duplicate giữa context mode và tool mode.
- Thêm unit test tối thiểu cho `AIConversationManager` (mock ChatHistoryManager) để đảm bảo create/load/set hoạt động đúng sau refactor.

## 6. Luồng xử lý đề xuất để giảm lỗi & tăng chính xác

1. **Chuẩn hoá trạng thái hội thoại**
   - `ConversationManager` giữ nguồn dữ liệu duy nhất (source of truth) cho mọi message.
   - `AIChatView` chỉ hiển thị, subscribe vào event `conversationManager.updated`.
   - Khi load history/stream hoàn tất → ghi message vào manager rồi UI render lại.
2. **Pipeline xử lý yêu cầu**
   1. Nhận input → validate, detect intent (cần tool nào?).
   2. Nếu cần data → ép `tool_choice:"required"` và gửi kèm system prompt: _“Trả lời dựa trên dữ liệu đã cấp, nếu thiếu hãy nói ‘Không đủ dữ liệu’.”_
   3. Khi provider trả `tool_calls`, chạy tools theo batch, append kết quả dưới dạng JSON chuẩn hóa (vd. `{ source:"project", data:[...] }`).
   4. Lặp tối đa 3 vòng; nếu vẫn cần tool → gửi message “context unavailable” và buộc LLM kết luận dựa trên dữ liệu đang có (không tự suy đoán).
   5. Trước khi hiển thị, post-process phản hồi: nếu assistant không dẫn nguồn (`Nguồn: …`) hoặc không có tool data đi kèm → gắn cảnh báo “Chưa kiểm chứng”.
3. **Chiến lược chống nói quá**
   - Template system prompt gồm:
     ```
     Bạn là trợ lý NoteFlix. Không tự bịa thông tin. Khi không tìm thấy dữ kiện, hãy trả lời
     "Tôi không có đủ dữ liệu trong hệ thống PCM để trả lời."
     ```
   - Với câu hỏi về hệ thống, luôn gắn `list_subsystems` + `search_projects` trước khi cho phép LLM trả lời tự do.
   - Khi user hỏi chung chung, kích hoạt “context injection nhẹ” (ví dụ chỉ summary subsystem) để tránh LLM suy luận quá sâu.
4. **Đề xuất function/tool mới**
   - `get_screen_details`, `search_events_by_keyword`, `list_projects_by_subsystem`, `get_recent_changes`, `summarize_chat`.
   - Mỗi tool trả về payload có metadata `source`, `timestamp` để LLM có thể cite trong câu trả lời.
5. **Xử lý lỗi/phản hồi**
   - Thiết lập retry (tối đa 2 lần) cho provider/tool; nếu vẫn fail → hiển thị toast “Không thể truy vấn dữ liệu, thử lại sau” + log analytics.
   - Streaming timeout kiểm soát: nếu không có chunk trong 10s → dừng stream, hỏi user có muốn retry.
6. **Audit & đánh giá**

- Log thêm flag `responseGrounded` (true/false) dựa trên việc assistant có cite source.
- Tạo job định kỳ rà log để phát hiện câu trả lời bị flag sai → dùng làm tập fine-tune hoặc prompt cải thiện.

## 7. Gợi ý xử lý ngữ nghĩa khi không dùng được Vector DB/Backend

1. **Từ điển đồng nghĩa nội bộ**
   - Duy trì file JSON client (`synonyms.json`) map các keyword phổ biến → danh sách biến thể (ví dụ: `{ "refund": ["hoàn tiền","trả tiền","refund"] }`).
   - Khi user nhập, chuẩn hóa chữ thường, bỏ dấu, tra map → mở rộng thành nhiều từ khóa để query bằng `LIKE`.
2. **Fuzzy search phía trình duyệt**
   - Tích hợp thư viện nhẹ như `Fuse.js` hoặc tự viết Levenshtein để so khớp xấp xỉ ngay trên IndexedDB data trước khi trả về kết quả.
   - Chi phí: tải thêm ~5-10KB JS, không cần backend.
3. **Chuẩn hoá văn bản trước khi lưu**
   - Lưu thêm field `normalized_name` (không dấu, lower-case) cho subsystem/project/screen → câu SQL `WHERE normalized_name LIKE '%${normalizedInput}%'` bắt được nhiều biến thể.
4. **Embedding mini on-device (tuỳ chọn)**
   - Nếu cần hơn, có thể chạy model sentence-transformer mini dạng WebAssembly (ví dụ `text-embedding-3-small` bản quantized hoặc `MiniLM`) ngay trong trình duyệt và lưu vector nhỏ trong IndexedDB. Tuy nhiên chi phí tải model ~20-30MB, nên chỉ bật qua feature flag.
5. **Prompt hỗ trợ đồng nghĩa**
   - Khi LLM hỏi lại tool (vd. user hỏi “phòng rủi ro”), ép tool `search_projects` nhận thêm `synonyms` từ dictionary để broaden truy vấn.
   - Nếu vẫn không tìm thấy, LLM trả lời “Không có kết quả trùng khớp nhưng các từ gần nghĩa bao gồm …” để hướng người dùng chỉnh lại.
6. **Analytics**

- Log những truy vấn không match để cập nhật từ điển thủ công (bảng csv `missing_keywords.csv`), giúp hệ thống học từ thực tế mà không cần BE.

## 8. Bổ sung để tăng độ chính xác & hiệu năng

1. **Chuẩn hoá & cắt bớt context**
   - Thiết lập `tokenBudget` (ví dụ 6k tokens). Khi lịch sử vượt, tự động gọi tool `summarize_chat` để rút gọn các đoạn cũ thành bullet cites → vừa giữ ngữ cảnh cốt lõi vừa giảm chi phí.
2. **Caching kết quả tool**
   - Với các hàm đọc dữ liệu tĩnh (list_subsystems, search_projects), lưu cache trong `sessionStorage` với TTL ngắn (5‑10 phút). Nếu LLM gọi lại cùng payload, trả từ cache để giảm IndexedDB I/O.
3. **Tách worker cho tìm kiếm nặng**
   - Các tác vụ fuzzy search hoặc thống kê lớn nên chạy trong Web Worker (ví dụ `search-worker.js`) để không block UI thread, nhất là khi người dùng kéo history dài.
4. **Đánh giá phản hồi hậu kỳ**
   - Áp dụng rule-based checker: sau khi LLM trả lời, script kiểm tra xem các thực thể (project/screen) có tồn tại trong DB hay không. Nếu không → gắn nhãn “cần xác minh” và khuyến khích người dùng phản hồi.
5. **Thống kê để tinh chỉnh prompt**
   - Ghi lại field `toolUsageCount`, `hallucinationFlag` để phân tích: câu nào không dùng tool mà vẫn trả lời → ưu tiên cải thiện system prompt/flow.
6. **Lazy load UI**
   - `AIHistoryModal` hiện render toàn bộ lịch sử ngay; có thể phân trang hoặc dùng `IntersectionObserver` để tải thêm khi cuộn, tránh giật với dataset lớn.
7. **Retry có chiến lược**
   - Thiết lập `retryPolicy` cho provider/tool (ví dụ 2 lần với backoff 500ms, 1s). Khi hết retry, trả lời rõ lý do (“Provider timeout/Tool error”) thay vì generic “Error: ...”.

## 9. Quản lý giới hạn tokens & gợi ý sửa code/database

1. **Khi gần vượt token limit**
   - Duy trì biến `tokenBudget` dựa trên `provider.settings.maxTokens`. Trước khi gọi LLM, ước lượng tokens của `system + history + context + user`. Nếu vượt:
     1. Gọi tool `summarize_chat` để nén các đoạn cũ thành đoạn tóm tắt kèm cite (giảm xuống ~200 tokens).
     2. Loại bỏ các message không chứa dữ liệu (chitchat, greeting).
     3. Giới hạn context injection còn top-N kết quả (ví dụ 3 screen/project) và tách phần chi tiết vào “phụ lục” mà LLM chỉ đọc khi cần.
   - Nếu sau khi cắt/summarize vẫn vượt → hiển thị cảnh báo “Hội thoại quá dài, hãy mở cuộc trò chuyện mới hoặc mô tả ngắn lại” thay vì gửi request chắc chắn fail.
2. **Phân trang history theo token**
   - Lưu `tokenEstimate` cho từng message (compute khi lưu). Khi cần build payload, gom từ cuối lên cho tới khi đạt ngưỡng; phần còn lại biểu diễn dưới dạng “summary chunk”.
   - Cho phép user bấm “Xem thêm lịch sử” để tải phần đã tóm tắt nếu muốn tham khảo.
3. **Hướng dẫn LLM gợi ý file/code/database cần sửa**
   - Tận dụng metadata sẵn có (screen -> `sourceFiles`, `databaseTables`). Sau khi LLM xác định scope, bắt buộc gọi tool `get_screen_details` hoặc tool mới `find_source_by_feature`.
   - Đề xuất bổ sung tool:
     - `find_source_by_feature({ keywords })` → trả danh sách `{ filePath, component, confidence }`.
     - `list_tables_by_feature({ keywords })` → trả bảng + cột liên quan.
     - `suggest_change_plan({ screenId, issue })` → worker client-side kết hợp metadata + rule (ví dụ: issue chứa “approval” → highlight `FinanceApproval.tsx`, bảng `payout_batches`).
   - Xây dựng trước file index (JSON) bằng script Node quét repo -> map keyword ↔ file/table. File này bundle cùng app và có thể được đồng bộ định kỳ.
   - Khi user nhập yêu cầu “sửa luồng risk review”, pipeline:
     1. Client trích từ khóa (“risk review”, “phê duyệt hoàn tiền”).
     2. Gọi `find_source_by_feature` + `list_tables_by_feature`.
     3. LLM nhận output, trả lời kiểu “Cần chỉnh `apps/refund-intake/src/pages/RiskReviewPage.tsx` và bảng `refund_audit_logs`…”, đồng thời nhắc người dùng xem xét migration nếu liên quan DB.
   - Với yêu cầu không match, LLM phải nói rõ “Chưa xác định được file/tables cụ thể” để tránh báo sai.

4. **Chia nhỏ resource khi nội dung quá rộng**
   - Khi người dùng yêu cầu phân tích một subsystem lớn (ví dụ toàn bộ “Revenue Recovery & Refunds”), thay vì cố nhồi 1 payload khổng lồ, có thể:
     1. **Chunking dữ liệu**: chia project/screen thành lô nhỏ (ví dụ mỗi chunk ≤ 1k tokens). Tool `get_project_chunk({ projectId, chunkIndex })` trả về phần dữ liệu tương ứng.
     2. **Iterative prompting**: LLM được hướng dẫn “hãy đọc chunk này, tóm tắt insight chính”, sau đó client ghép kết quả từng chunk thành danh sách.
     3. **Tổng hợp cuối**: sau khi thu thập hết, gửi prompt cuối cùng gồm các summary ngắn + câu hỏi người dùng để sinh output tổng hợp.
   - Mô hình pipeline:
     ```
     for chunk in chunks:
       summary_i = LLM("Tóm tắt chunk i ...")
     final_answer = LLM("Dựa trên các summary 1..n, trả lời câu hỏi người dùng ...")
     ```
   - Ưu điểm: không cần vector DB; chi phí chia nhỏ nằm ở client (IndexedDB + worker) nên vẫn chạy offline.
   - Cần caching summary từng chunk để khi người dùng hỏi lại, không phải gọi LLM lần nữa.

---

Sau khi team chọn hướng giải quyết, có thể tạo ticket tương ứng trong backlog “AI Panel hardening” để theo dõi triển khai.

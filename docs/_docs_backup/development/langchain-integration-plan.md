# Kế hoạch tích hợp LangChain.js vào `apps/pcm-webapp`

## Mục tiêu

- Kích hoạt khả năng **tool calling** để LLM có thể gọi các công cụ nội bộ (truy vấn IndexedDB, sinh báo cáo, tra cứu tài liệu…).
- Hoạt động ổn định trong môi trường **kết nối mạng hạn chế**, ưu tiên sử dụng mô hình/nguồn dữ liệu cục bộ.
- Sử dụng các phiên bản LangChain mới nhất (thời điểm 2024-11):
  - `langchain@0.3.x`
  - `@langchain/core@0.3.x`
  - Các package theo nhu cầu: `@langchain/community`, `@langchain/openai`, `@langchain/ollama`, `@langchain/google-genai`, …

## 1. Chuẩn bị hạ tầng dự án

1. Bổ sung `package.json` (nếu chưa có) cho `apps/pcm-webapp`, cấu hình Vite/React hoặc framework hiện hữu.
2. Cài đặt phụ thuộc (sử dụng pnpm):

   ```bash
   pnpm add -D @types/node
   pnpm add langchain@0.3.7 @langchain/core@0.3.7 @langchain/community@0.3.7
   pnpm add @langchain/ollama@0.1.2 @langchain/openai@0.3.6
   pnpm add zod@^3.23 uuid
   ```

   > _Phiên bản có thể cập nhật tuỳ thời điểm; dùng `pnpm info langchain version` để kiểm tra mới nhất._

3. Thiết lập alias/module resolution nếu dự án dùng TypeScript (tsconfig paths).
4. Cấu hình build để hỗ trợ dynamic import cho các provider (Vite alias `process.env`).

## 2. Kiến trúc đề xuất

```
apps/pcm-webapp/
  src/
    agents/
      agentRegistry.ts      # Đăng ký agent/tool
      pcmAgent.ts           # Agent chính kết hợp tool calling
    llm/
      providers.ts          # Khởi tạo Chat Models (Ollama, OpenAI…)
      embeddings.ts         # Embedding cho indexed data
    tools/
      index.ts              # Export tool list
      queryIndexedDb.ts     # Tool đọc IndexedDB
      reportGenerator.ts    # Tool tạo báo cáo nhanh
      planningTool.ts       # Tool lập kế hoạch
    vectorstore/
      inBrowserMemory.ts    # Vector store chạy trong trình duyệt
    workflows/
      qaWorkflow.ts         # Chuỗi RAG hỏi đáp
    utils/
      env.ts                # Đọc config (API key, base URL)
      cache.ts              # Lưu cache kết quả vào IndexedDB/localStorage
```

### Luồng tương tác

1. UI nhận câu hỏi từ người dùng → gửi tới `pcmAgent`.
2. Agent dùng `RunnableWithMessageHistory` + `ToolCallingAgent` của LangChain để quyết định gọi tool.
3. Tool `queryIndexedDb` khai thác dữ liệu cục bộ trong IndexedDB.
4. Nếu cần embedding (QA), sử dụng `MemoryVectorStore` (in-memory) + lưu snapshot embedding vào IndexedDB để tái sử dụng.
5. Kết quả được định dạng bằng `StructuredOutputParser` rồi trả về UI.

## 3. Tích hợp LLM trong môi trường hạn chế mạng

### 3.1 Ưu tiên mô hình cục bộ (Ollama, LM Studio)

- Sử dụng `@langchain/ollama` với model `llama3`, `phi3`, `qwen2` tùy hạ tầng.
- Config:

  ```ts
  import { ChatOllama } from "@langchain/ollama";

  const chatModel = new ChatOllama({
    baseUrl: env.OLLAMA_BASE_URL || "http://localhost:11434",
    model: env.OLLAMA_MODEL || "llama3",
    streaming: true,
  });
  ```

- Embedding có thể dùng `@langchain/ollama/embeddings`.

### 3.2 Fallback mạng hạn chế

- Nếu không truy cập được server cục bộ, chuyển sang mô hình cloud (`ChatOpenAI`, `ChatGoogleGenerativeAI`) thông qua cấu hình runtime.
- Dùng `AbortController` + timeout để fallback tự động.

### 3.3 Caching & Prefetch

- Cache embedding và chunk dữ liệu vào IndexedDB.
- Sử dụng `langchain/cache` với `InMemoryCache` hoặc tự viết `PersistentCache`.
- Chuẩn bị script pre-warm: khi user đăng nhập lần đầu, hệ thống scan dữ liệu, chunk, tính embedding.

## 4. Tool Calling

### Ví dụ tool truy vấn IndexedDB

```ts
import { DynamicStructuredTool } from "@langchain/core/tools";
import { z } from "zod";
import { queryProjectData } from "../services/indexedDbAdapter";

export const queryIndexedDbTool = new DynamicStructuredTool({
  name: "query_project_data",
  description: "Truy vấn dữ liệu dự án PCM trong IndexedDB theo từ khoá.",
  schema: z.object({
    collection: z.string(),
    filter: z.string().describe("Từ khoá hoặc biểu thức tìm kiếm"),
  }),
  func: async ({ collection, filter }) => {
    const results = await queryProjectData(collection, filter);
    return JSON.stringify(results.slice(0, 5));
  },
});
```

### Tạo agent hỗ trợ tool calling

```ts
import { RunnableWithMessageHistory } from "@langchain/core/runnables";
import { createToolCallingAgent } from "langchain/agents";
import { chatPrompt } from "../prompts/toolCallingPrompt";

export const createPcmAgent = (llm, tools, memory) => {
  const agent = createToolCallingAgent({ llm, tools, prompt: chatPrompt });
  return new RunnableWithMessageHistory({
    runnable: agent,
    history: memory,
  });
};
```

## 5. RAG nhẹ trong trình duyệt

- Dùng `langchain/vectorstores/memory` để giữ embedding trong session.
- Sync embedding xuống IndexedDB → khi reload, load vào memory.
- Chunk dữ liệu `docs/*.md`, `screens`, `notes` (tận dụng script có sẵn trong repo `docs`).
- Lựa chọn embedding:
  - `OllamaEmbeddings` (offline).
  - Hoặc `OpenAIEmbeddings` (khi có mạng).

## 6. UI/UX

- Tab “AI Assistant” trong `pcm-webapp` với các tính năng:
  1. Q&A (sử dụng RAG).
  2. Command mode (giao tool calling: `summarize`, `generate_report`, `list_deadlines`).
  3. Log hiển thị tool nào đã được agent gọi.

- Thêm tùy chọn cấu hình trong UI: chọn mô hình, chế độ offline/online.

## 7. Bảo mật & giới hạn mạng

- Tách phần gọi API ra `worker` hoặc `serverless function` nếu cần bảo vệ API key.
- Cho phép import/export cấu hình (baseUrl, API key) qua file JSON để không phải nhập nhiều lần.
- Nếu môi trường cực kỳ hạn chế, chuẩn bị một gói “model weights + embedding script” để chạy hoàn toàn cục bộ.

## 8. Lộ trình triển khai

1. Thiết lập môi trường (package.json, pnpm, tsconfig).
2. Viết adapter IndexedDB + script chunk/embedding.
3. Tạo các tool cơ bản: truy vấn dữ liệu, sinh báo cáo, tạo kế hoạch.
4. Tích hợp LangChain agent + UI.
5. Thử nghiệm với mô hình Ollama offline → fallback cloud.
6. Hoàn thiện UX (loading state, hiển thị nguồn, ghi log tool call).
7. Viết tài liệu hướng dẫn sử dụng & cấu hình.

## 9. Khuyến nghị mở rộng

- Khi nhu cầu tăng, cân nhắc chuyển sang vector database chuyên dụng (Weaviate, Pinecone) nhưng giữ nguyên API tool.
- Tích hợp thêm `langgraph` để mô hình hoá workflow phức tạp.
- Tạo bộ test tự động cho agent (dùng LangChain smoke tests).

---

**Kết luận:** Kế hoạch sử dụng LangChain.js mới nhất kết hợp tool calling, ưu tiên mô hình cục bộ (Ollama) giúp `apps/pcm-webapp` hoạt động tốt trong môi trường mạng hạn chế. Việc modular hoá các provider và tool giúp mở rộng dễ dàng khi điều kiện hạ tầng thay đổi.

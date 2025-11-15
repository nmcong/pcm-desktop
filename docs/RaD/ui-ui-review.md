# UI Module Review Report

Repository: `pcm-desktop`  
Scope: `src/main/java/com/noteflix/pcm/ui`

---

## 1. Schema tree never updates after data load

- **Evidence:** `DatabaseObjectsPage` populates the tree via `updateSchemaTree()` only once during construction (`DatabaseObjectsPage.java:131-165`), yet `onPageActivated()` later invokes the asynchronous `DatabaseObjectsViewModel.loadDatabaseInfo()` (`DatabaseObjectsPage.java:352`). The view never observes `schemaObjects`/`databaseName`, so the tree stays empty even when the ViewModel succeeds.
- **Impact:** Users never see database objects after activation or refresh, defeating the purpose of the page.
- **Proposed Fix:** Observe `viewModel.getSchemaObjects()` and `databaseNameProperty()` (either with `ListChangeListener` or `InvalidationListener`) and call `updateSchemaTree()` whenever they change. Alternatively, build the tree directly from the observable list so modifications propagate automatically.

---

## 2. Object detail panel never reflects selection

- **Evidence:** Tree selection triggers `viewModel.selectSchemaObject()` (`DatabaseObjectsPage.java:140-147`), but `objectDetails` is never updated afterwards; `showObjectDetails()` (`DatabaseObjectsPage.java:221-257`) is unused and there is no binding to `selectedObjectDetailsProperty`.
- **Impact:** Clicking a schema node yields no visible feedback, so users cannot inspect table columns, indexes, etc.
- **Proposed Fix:** Add a listener to `viewModel.selectedObjectDetailsProperty()` (or expose richer data) and update `objectDetails` accordingly—either by calling `showObjectDetails` with the selected metadata or by rendering the raw description text. Ensure the pane clears/updates on selection change.

---

## 3. AI chat messages never persist

- **Evidence:** In `AIAssistantPage.handleSendMessage()` (`AIAssistantPage.java:464-540`) the UI shows the user message and streams the AI reply, but the code never calls `ConversationService.sendMessage`/`addMessageToConversation`. When `loadMessages()` runs (line 620) it reloads the conversation from the repository, which still contains the old state, so the freshly displayed bubbles vanish and previews/token counts never progress.
- **Impact:** Conversation history resets after every refresh, sidebar previews remain stale, and analytics derived from persisted messages are incorrect.
- **Proposed Fix:** Persist both sides of the exchange before updating the UI. Either delegate the entire flow to `ConversationService.sendMessage` or call `addMessageToConversation` for the user message and append the streamed assistant message at completion. Consider reusing `AIAssistantViewModel`, which already encapsulates this logic.

---

## 4. Chat service calls block the JavaFX thread

- **Evidence:** Methods such as `handleSearch`, `loadConversations`, `loadMessages`, and `clearCurrentChat` (`AIAssistantPage.java:552-624`) call repository-backed services synchronously and `handleSearch` fires on every keystroke. These operations touch disk (SQLite) or network (LLM) through `ConversationService`, yet they run directly on the FX thread.
- **Impact:** Any slow response freezes the UI, especially while streaming or searching large histories.
- **Proposed Fix:** Move service invocations onto background threads using the existing `Asyncs` helper or, better, reuse the already-written `AIAssistantViewModel` which exposes asynchronous commands. Add debouncing to the search box to avoid hammering the service on every character.

---

## 5. Settings listeners registered repeatedly

- **Evidence:** `SettingsPage.onPageActivated()` calls `SettingsViewModel.loadSettings()` every time the page is shown (`SettingsPage.java:49-52`). That method attaches new listeners to `selectedTheme` and `selectedLanguage` without guarding against duplicates (`SettingsViewModel.java:42-65`), so multiple identical listeners accumulate.
- **Impact:** Re-entering the page causes theme toggles and locale changes to execute multiple times, and the listeners leak indefinitely.
- **Proposed Fix:** Register listeners once (e.g., inside the constructor) or guard `loadSettings()` with a boolean flag. Optionally remove listeners before re-adding them.

---

Please let me know if you would like implementation help or test suggestions for any of the fixes above.

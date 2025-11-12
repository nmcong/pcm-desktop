# AI Provider System - Auto-Generated UI

## 🎯 Mục tiêu đã đạt được

Hệ thống AI Provider hiện đã **HOÀN TOÀN TỰ ĐỘNG HÓA**:

- ✅ API Keys section tự động generate dựa trên providers
- ✅ Conversation settings tự động thay đổi theo provider được chọn
- ✅ Thêm provider mới không cần sửa AIPanel
- ✅ Mỗi provider có cấu hình và settings riêng

---

## 🏗️ Kiến trúc

### 1. BaseProvider (Abstract Class)

**Location**: `public/js/services/ai/BaseProvider.js`

**Mới thêm**:

```javascript
// API Key Configuration
this.apiKeyConfig = {
  required: true, // Có cần API key không?
  storageKey: "provider-api-key", // Key trong localStorage
  label: "Provider API Key", // Label hiển thị
  placeholder: "Enter API key", // Placeholder
  hint: "Get your key from...", // Hint text
};

// Settings Configuration (định nghĩa settings mà provider hỗ trợ)
this.settings = {
  maxTokens: {
    supported: true, // Provider có hỗ trợ không?
    min: 256,
    max: 8192,
    default: 2048,
    step: 256,
  },
  temperature: {
    supported: true,
    min: 0,
    max: 1,
    default: 0.7,
    step: 0.1,
  },
  thinking: {
    supported: false, // Provider có hỗ trợ thinking mode không?
  },
};
```

**Methods mới**:

- `loadApiKey()`: Tự động load API key từ localStorage
- `saveApiKey(key)`: Tự động save API key vào localStorage

---

### 2. AIPanel - Auto-Generated UI

**Location**: `public/js/components/AIPanel.js`

#### A. createSettingsForm() - 100% Auto

```javascript
createSettingsForm() {
  // 1. Provider Selection (như cũ)

  // 2. API Keys Section - AUTO GENERATED
  const apiKeyInputs = providers
    .filter(p => p.apiKeyConfig.required)  // Chỉ show providers cần API key
    .map(p => `
      <input
        id="${p.id}-api-key"           // ID tự động
        placeholder="${p.apiKeyConfig.placeholder}"
        value="${localStorage.getItem(p.apiKeyConfig.storageKey)}"
      />
      <span class="form-hint">${p.apiKeyConfig.hint}</span>
    `);

  // 3. Conversation Settings - DYNAMIC
  this.updateConversationSettingsSection(settingsSection, activeProvider);
}
```

#### B. updateConversationSettingsSection() - Dynamic UI

```javascript
updateConversationSettingsSection(section, provider) {
  let html = "";

  // Chỉ render settings mà provider hỗ trợ
  if (provider.settings.maxTokens.supported) {
    html += `<input type="number"
              min="${provider.settings.maxTokens.min}"
              max="${provider.settings.maxTokens.max}" ... />`;
  }

  if (provider.settings.temperature.supported) {
    html += `<input type="range"
              min="${provider.settings.temperature.min}"
              max="${provider.settings.temperature.max}" ... />`;
  }

  if (provider.settings.thinking.supported) {
    html += `<input type="checkbox" ... />`;
  }

  section.innerHTML = html;
}
```

#### C. saveSettings() - Auto save ALL providers

```javascript
saveSettings(form) {
  // 1. Set active provider
  providerRegistry.setActive(providerId);

  // 2. Save ALL API keys automatically
  providers.forEach(provider => {
    if (provider.apiKeyConfig.required) {
      const input = form.querySelector(`#${provider.id}-api-key`);
      if (input?.value) {
        provider.saveApiKey(input.value);  // Tự động save
      }
    }
  });

  // 3. Save settings for active provider
  const settings = {};
  if (activeProvider.settings.maxTokens.supported) {
    settings.maxTokens = form.querySelector("#max-tokens").value;
  }
  // ... các settings khác

  this.saveProviderSettings(activeProvider.id, settings);
}
```

#### D. Settings Persistence

```javascript
// Mỗi provider có settings riêng
saveProviderSettings(providerId, settings) {
  localStorage.setItem(`ai-provider-settings-${providerId}`, JSON.stringify(settings));
}

loadProviderSettings(providerId) {
  const saved = localStorage.getItem(`ai-provider-settings-${providerId}`);
  return saved ? JSON.parse(saved) : {};
}
```

---

### 3. Provider Implementations

#### MockProvider

```javascript
apiKeyConfig: {
  required: false,  // Không cần API key
}
settings: {
  maxTokens: { supported: true, default: 2048 },
  temperature: { supported: true, default: 0.7 },
  thinking: { supported: true },
}
```

#### ViByteProvider

```javascript
apiKeyConfig: {
  required: true,
  storageKey: "vibyte-ai-api-key",
  label: "ViByte Cloud API Key",
  hint: "Get your API key from ViByte Cloud Dashboard",
}
settings: {
  maxTokens: { supported: true, min: 256, max: 8192 },
  temperature: { supported: true, min: 0, max: 2 },
  thinking: { supported: true },
}
```

#### OpenAIProvider (Demo)

```javascript
apiKeyConfig: {
  required: true,
  storageKey: "openai-api-key",
  label: "OpenAI API Key",
  placeholder: "sk-...",
  hint: "Get your API key from platform.openai.com",
}
settings: {
  maxTokens: { supported: true, min: 1, max: 4096 },
  temperature: { supported: true, min: 0, max: 2 },
  thinking: { supported: false },  // OpenAI không có thinking mode
}
```

---

## 🚀 Cách thêm Provider mới

### Bước 1: Tạo Provider Class

```javascript
// public/js/services/ai/NewProvider.js
export class NewProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      id: "new-provider",
      name: "New Provider",
      apiKeyConfig: { ... },      // Tự động tạo input
      capabilities: { ... },       // Hiển thị badges
      settings: { ... },           // Tự động tạo settings UI
    });
    this.loadApiKey();
  }

  async chat(messages, options) { ... }
  async *streamChat(messages, options) { ... }
}
```

### Bước 2: Register Provider

```javascript
// public/js/services/ai/ProviderRegistry.js
import { NewProvider } from "./NewProvider.js";

registerDefaultProviders() {
  this.register(new NewProvider());  // XONG!
}
```

**Không cần sửa gì thêm!** UI tự động tạo:

- ✅ API key input với label, placeholder, hint
- ✅ Provider trong danh sách với capabilities badges
- ✅ Settings phù hợp với provider capabilities

---

## 📦 Files đã thay đổi

### Modified Files

1. ✅ `public/js/services/ai/BaseProvider.js`
   - Added `apiKeyConfig` configuration
   - Added `settings` configuration
   - Added `loadApiKey()` / `saveApiKey()` methods

2. ✅ `public/js/services/ai/MockProvider.js`
   - Added `apiKeyConfig` (required: false)
   - Added `settings` configuration

3. ✅ `public/js/services/ai/ViByteProvider.js`
   - Added `apiKeyConfig` with ViByte-specific config
   - Added `settings` configuration
   - Auto-loads API key on initialization

4. ✅ `public/js/services/ai/ProviderRegistry.js`
   - Removed hardcoded API key loading

5. ✅ `public/js/components/AIPanel.js`
   - `createSettingsForm()`: Auto-generates API key inputs
   - `updateConversationSettingsSection()`: Dynamic settings UI
   - `saveSettings()`: Auto-saves all provider API keys
   - `saveProviderSettings()` / `loadProviderSettings()`: Per-provider settings
   - `setupTemperatureSlider()`: Helper method

### New Files

6. ✅ `public/js/services/ai/OpenAIProvider.js`
   - Complete OpenAI implementation as example
7. ✅ `docs/ADD_NEW_AI_PROVIDER.md`
   - Comprehensive guide for adding new providers
8. ✅ `docs/AI_PROVIDER_SYSTEM_SUMMARY.md`
   - This file

---

## 🎨 UI Flow

### Settings Modal Flow

```
User opens AI Settings
├─ Provider List (all registered providers)
│  ├─ Provider 1 [active] + capabilities badges
│  ├─ Provider 2 + capabilities badges
│  └─ Provider 3 + capabilities badges
│
├─ API Keys Section (AUTO GENERATED)
│  ├─ Provider 1 API Key [only if required: true]
│  ├─ Provider 2 API Key [only if required: true]
│  └─ (Provider 3 không hiển thị vì required: false)
│
└─ Conversation Settings (DYNAMIC based on selected provider)
   ├─ Max Tokens [if supported]
   ├─ Temperature [if supported]
   └─ Thinking Mode [if supported]

User clicks different provider
└─> Conversation Settings section updates automatically
    └─> Min/max/step values adjust to new provider's config
```

### Settings Persistence

```
localStorage
├─ ai-active-provider: "vibyte-cloud"
├─ vibyte-ai-api-key: "your-key-here"
├─ openai-api-key: "sk-..."
├─ ai-provider-settings-vibyte-cloud: { maxTokens: 2048, temperature: 0.7, thinking: true }
└─ ai-provider-settings-openai: { maxTokens: 4096, temperature: 1 }
```

---

## ✨ Benefits

### 1. Zero AIPanel Modifications

Thêm 10 providers mới → Không cần sửa AIPanel

- API key inputs tự động tạo
- Settings UI tự động adapt

### 2. Type-Safe Configuration

Provider config là single source of truth:

- `apiKeyConfig` → UI generation
- `settings` → Input validation + UI
- `capabilities` → Feature availability

### 3. DRY Principle

Không duplicate code:

- API key handling: 1 nơi (BaseProvider)
- Settings rendering: 1 method (updateConversationSettingsSection)
- Persistence: 1 pattern (saveProviderSettings)

### 4. Flexible & Extensible

Mỗi provider tự do:

- Có hoặc không có API key
- Min/max khác nhau
- Hỗ trợ hoặc không hỗ trợ thinking mode

### 5. User-Friendly

- API keys tự động load/save
- Settings per provider
- Input validation dựa trên provider config

---

## 🧪 Testing

### Test Case 1: Mock Provider

1. Open Settings
2. Select "Mock AI"
3. ✅ **No API key input** (required: false)
4. ✅ Max Tokens: 256-8192
5. ✅ Temperature: 0-1
6. ✅ Thinking mode: available

### Test Case 2: ViByte Provider

1. Select "ViByte Cloud AI"
2. ✅ **API key input visible** with hint
3. ✅ Max Tokens: 256-8192
4. ✅ Temperature: 0-2 (wider range)
5. ✅ Thinking mode: available

### Test Case 3: OpenAI Provider (if enabled)

1. Select "OpenAI"
2. ✅ **API key input** with "sk-..." placeholder
3. ✅ Max Tokens: 1-4096 (different range)
4. ✅ Temperature: 0-2
5. ✅ **No thinking mode** (supported: false)

### Test Case 4: Settings Persistence

1. Select ViByte, set maxTokens=4000, save
2. Select OpenAI, set maxTokens=2000, save
3. Switch back to ViByte
4. ✅ maxTokens is still 4000 (not 2000)

---

## 🎓 How It Works

### Architecture Pattern: **Configuration-Driven UI**

```
Provider Configuration (Data)
         ↓
   AIPanel reads config
         ↓
   Generates UI automatically
         ↓
   User interacts
         ↓
   Saves per provider
```

**Key Insight**:

- Provider = Configuration object
- AIPanel = Rendering engine
- No hardcoded provider knowledge in UI

---

## 📝 Summary

Hệ thống đã được thiết kế để:

1. **Tự động hóa hoàn toàn** việc tạo UI cho providers mới
2. **Phân tách rõ ràng** giữa configuration (providers) và presentation (AIPanel)
3. **Dễ dàng mở rộng** không giới hạn số lượng providers
4. **An toàn về kiểu** với TypeScript-friendly configuration
5. **Thân thiện người dùng** với auto-save và per-provider settings

**Kết quả**: Thêm AI provider mới chỉ cần 2 bước (tạo class + register), không cần động vào UI code!

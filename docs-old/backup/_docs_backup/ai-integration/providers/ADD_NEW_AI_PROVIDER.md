# How to Add a New AI Provider

This guide shows how to add a new AI provider to the PCM WebApp **without modifying AIPanel**.

## Architecture Benefits

The system is fully automatic:

- ✅ **API Key inputs** are auto-generated based on provider configuration
- ✅ **Conversation settings** automatically adapt to provider capabilities
- ✅ **No AIPanel modifications** needed when adding new providers
- ✅ **Provider-specific settings** are saved and loaded automatically

---

## Step 1: Create Your Provider Class

Create a new file in `public/js/services/ai/YourProvider.js`:

```javascript
import { BaseProvider } from "./BaseProvider.js";

export class YourProvider extends BaseProvider {
  constructor(config = {}) {
    super({
      // Basic info
      id: "your-provider", // Unique ID
      name: "Your Provider Name", // Display name
      baseURL: "https://api.yourprovider.com",

      // API Key configuration (AUTOMATIC UI generation)
      apiKeyConfig: {
        required: true, // If false, no API key input will be shown
        storageKey: "your-provider-api-key", // localStorage key
        label: "Your Provider API Key", // Label in settings
        placeholder: "Enter your API key", // Input placeholder
        hint: "Get your key from your-provider.com", // Hint text
      },

      // Capabilities (shown as badges)
      capabilities: {
        chat: true, // Can chat
        generate: true, // Can generate text
        stream: true, // Supports streaming
        thinking: false, // Supports thinking mode
        vision: false, // Supports vision/images
        tools: false, // Supports function calling
      },

      // Settings configuration (AUTOMATIC UI generation)
      settings: {
        maxTokens: {
          supported: true, // If false, no max tokens input
          min: 256,
          max: 8192,
          default: 2048,
          step: 256,
        },
        temperature: {
          supported: true, // If false, no temperature slider
          min: 0,
          max: 2,
          default: 0.7,
          step: 0.1,
        },
        thinking: {
          supported: false, // If false, no thinking checkbox
        },
      },

      // Rate limiting
      rateLimit: {
        requestsPerMinute: 60,
        tokensPerMinute: 90000,
      },

      ...config,
    });

    // Load saved API key from localStorage
    this.loadApiKey();
  }

  // Implement required methods
  async chat(messages, options = {}) {
    // Your implementation
  }

  async *streamChat(messages, options = {}) {
    // Your streaming implementation
  }

  async healthCheck() {
    // Check if provider is healthy
  }

  async getModels() {
    // Get available models
  }
}
```

---

## Step 2: Register Your Provider

In `public/js/services/ai/ProviderRegistry.js`, add your provider:

```javascript
import { YourProvider } from "./YourProvider.js";

registerDefaultProviders() {
  // Existing providers
  this.register(new MockProvider());
  this.register(new ViByteProvider({ ... }));

  // Add your provider (THAT'S IT!)
  this.register(new YourProvider({
    baseURL: "https://api.yourprovider.com",
  }));
}
```

---

## That's It!

Your provider is now fully integrated:

### Automatic UI Generation

1. **API Key Input**: Automatically appears in Settings → API Keys section
    - Label, placeholder, and hint from `apiKeyConfig`
    - Saved/loaded automatically via `saveApiKey()` / `loadApiKey()`

2. **Provider Selection**: Shows in provider list with capabilities badges

3. **Dynamic Settings**: When user selects your provider:
    - Max Tokens input adjusts to your min/max/step
    - Temperature slider adjusts to your range
    - Thinking mode checkbox shows/hides based on support

### Settings Persistence

- Each provider has separate settings in localStorage
- Key format: `ai-provider-settings-{provider-id}`
- Automatically loaded when provider becomes active

---

## Example: OpenAI Provider

See `public/js/services/ai/OpenAIProvider.js` for a complete example.

**To enable OpenAI:**

1. Uncomment the import in `ProviderRegistry.js`
2. Uncomment the registration line
3. Reload the page
4. OpenAI now appears in Settings with its own API key input

---

## API Key Configuration Options

```javascript
apiKeyConfig: {
  required: true,              // Show API key input? (false = no input)
  storageKey: "custom-key",    // localStorage key
  label: "Display Label",      // Form label
  placeholder: "Enter...",     // Input placeholder
  hint: "Help text here",      // Hint below input
}
```

**If `required: false`**: No API key input is generated (useful for local/mock providers)

---

## Settings Configuration Options

### Max Tokens

```javascript
maxTokens: {
  supported: true,      // Show input?
  min: 1,
  max: 100000,
  default: 2048,
  step: 1,
}
```

### Temperature

```javascript
temperature: {
  supported: true,      // Show slider?
  min: 0,
  max: 2,
  default: 0.7,
  step: 0.1,
}
```

### Thinking Mode

```javascript
thinking: {
  supported: true,      // Show checkbox?
}
```

---

## Benefits

✅ **No AIPanel modifications** - Add unlimited providers without touching UI code  
✅ **Automatic validation** - Min/max enforced by browser inputs  
✅ **Type-safe** - Provider config drives UI generation  
✅ **DRY principle** - Configuration is single source of truth  
✅ **Flexible** - Each provider can have different capabilities and settings

---

## Testing Your Provider

1. Open AI Panel (Ctrl/Cmd + K)
2. Click Settings icon
3. Your provider should appear in the list
4. Select it → conversation settings update automatically
5. Enter API key → saved to localStorage automatically
6. Click Save → settings persisted per provider

---

## Advanced: Custom Settings

You can add custom settings by extending the `settings` object:

```javascript
settings: {
  // Standard settings
  maxTokens: { ... },
  temperature: { ... },
  thinking: { ... },

  // Custom settings (you'll need to handle UI manually)
  topP: {
    supported: true,
    min: 0,
    max: 1,
    default: 0.9,
  },
  presencePenalty: {
    supported: true,
    min: -2,
    max: 2,
    default: 0,
  },
}
```

For custom settings, you can either:

1. Extend `updateConversationSettingsSection()` in AIPanel
2. Or handle them in your provider's `chat()` method

---

## Summary

```
1. Create YourProvider.js extending BaseProvider
2. Configure apiKeyConfig and settings
3. Register in ProviderRegistry
4. Done! UI auto-generates everything.
```

**The AIPanel never needs to know about your provider's specifics!**

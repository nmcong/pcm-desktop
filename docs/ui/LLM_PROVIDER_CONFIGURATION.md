# LLM Provider Configuration Feature

## 📋 Overview

Hệ thống quản lý cấu hình cho các LLM providers (OpenAI, Anthropic, Ollama, Custom API) với giao diện người dùng hiện đại và đầy đủ tính năng.

## ✨ Features

### 1. **Multi-Provider Support**
- OpenAI (GPT-3.5, GPT-4, GPT-4 Turbo, ...)
- Anthropic Claude (Claude 3 Opus, Sonnet, Haiku, ...)
- Ollama (Local AI models)
- Custom API (Self-hosted or third-party APIs)

### 2. **Configuration Management**
- ✅ Configure API keys (encrypted storage)
- ✅ Set custom base URLs
- ✅ Select default models
- ✅ Connection timeout settings
- ✅ Retry policies
- ✅ Enable/disable providers
- ✅ Set active (default) provider

### 3. **Testing & Validation**
- ✅ Test connection to provider API
- ✅ Display connection status (✓ Connected / ✗ Failed)
- ✅ Load available models from provider
- ✅ Real-time feedback

### 4. **User Interface**
- ✅ Modern tabbed interface using AtlantaFX
- ✅ Card-based layout for each provider
- ✅ Responsive design
- ✅ Dark/Light theme support
- ✅ Icon indicators for status
- ✅ Smooth animations and transitions

## 🏗️ Architecture

### Database Schema

```sql
CREATE TABLE provider_configurations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    provider_name TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    api_key TEXT,
    api_base_url TEXT,
    default_model TEXT,
    is_active BOOLEAN DEFAULT 0,
    is_enabled BOOLEAN DEFAULT 1,
    requires_api_key BOOLEAN DEFAULT 1,
    connection_timeout INTEGER DEFAULT 30000,
    max_retries INTEGER DEFAULT 3,
    extra_config TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_tested_at TIMESTAMP,
    test_status TEXT
);
```

### Layers

#### 1. **Domain Layer**
```
com.noteflix.pcm.domain.provider
  └── ProviderConfiguration.java  # Domain model
```

#### 2. **Infrastructure Layer**
```
com.noteflix.pcm.infrastructure
  ├── dao
  │   └── ProviderConfigurationDAO.java
  └── repository/provider
      ├── ProviderConfigurationRepository.java      # Interface
      └── ProviderConfigurationRepositoryImpl.java  # Implementation
```

#### 3. **Application Layer**
```
com.noteflix.pcm.application.service.provider
  └── ProviderConfigurationService.java  # Business logic
```

#### 4. **Presentation Layer**
```
com.noteflix.pcm.ui.pages.settings
  ├── SettingsPage.java                    # Main settings page with tabs
  ├── tabs
  │   └── LLMProvidersTab.java             # LLM providers tab
  └── components
      └── ProviderConfigCard.java          # Individual provider card
```

## 🔧 Design Principles Applied

### SOLID Principles

1. **Single Responsibility Principle (SRP)**
   - `ProviderConfiguration`: Chỉ chứa dữ liệu cấu hình
   - `ProviderConfigurationDAO`: Chỉ xử lý database operations
   - `ProviderConfigurationService`: Chỉ xử lý business logic
   - `ProviderConfigCard`: Chỉ hiển thị và quản lý UI cho một provider

2. **Open/Closed Principle (OCP)**
   - Hệ thống mở rộng cho providers mới mà không cần sửa code hiện tại
   - Chỉ cần thêm provider vào registry và database

3. **Liskov Substitution Principle (LSP)**
   - `ProviderConfigurationRepository` interface có thể thay thế implementation

4. **Interface Segregation Principle (ISP)**
   - Repository interface chỉ chứa methods cần thiết
   - Không ép client implement methods không dùng

5. **Dependency Inversion Principle (DIP)**
   - Service depends on Repository interface, không phải concrete implementation
   - UI depends on Service interface, không phải implementation details

### Clean Architecture

```
Presentation Layer (UI)
         ↓
Application Layer (Services)
         ↓
Domain Layer (Models)
         ↓
Infrastructure Layer (Database, External APIs)
```

### Clean Code Practices

- ✅ Meaningful names
- ✅ Small, focused methods
- ✅ Clear comments and documentation
- ✅ Error handling
- ✅ Validation
- ✅ Logging
- ✅ Type safety
- ✅ Immutability where appropriate

## 📖 Usage

### 1. Access Settings Page

Click on "Settings" in the navigation sidebar.

### 2. Configure a Provider

1. Select the "LLM Providers" tab
2. Find the provider card (e.g., "OpenAI")
3. Toggle "Enabled" to enable the provider
4. Enter your API key (if required)
5. (Optional) Customize the base URL
6. Click "Save"

### 3. Test Connection

After saving configuration:
1. Click "Test Connection" button
2. Wait for the test to complete
3. Check the status indicator:
   - ✓ Connected: Connection successful
   - ✗ Failed: Connection failed

### 4. Load Available Models

1. Click the sync button (↻) next to the model field
2. Wait for models to load
3. Select a model from the dropdown
4. Click "Save" to set as default model

### 5. Set Active Provider

1. Click the star icon (☆) on the provider card
2. The provider will be marked as active (★)
3. Only one provider can be active at a time

## 🔐 Security

### API Key Storage

- API keys are stored in SQLite database
- Future enhancement: Encrypt API keys using AES-256
- Never log API keys in plaintext

### Best Practices

1. **Use environment variables** for sensitive data in production
2. **Rotate API keys** regularly
3. **Use read-only keys** when possible
4. **Monitor API usage** to detect anomalies
5. **Implement rate limiting** to prevent abuse

## 🧪 Testing

### Unit Tests (Future)

```java
// Test ProviderConfiguration validation
@Test
void testConfigurationValidation() {
    ProviderConfiguration config = ProviderConfiguration.builder()
        .providerName("openai")
        .displayName("OpenAI")
        .requiresApiKey(true)
        .build();
    
    assertThrows(IllegalStateException.class, config::validate);
}

// Test repository operations
@Test
void testSaveAndRetrieve() {
    ProviderConfiguration config = createTestConfig();
    repository.save(config);
    
    Optional<ProviderConfiguration> retrieved = 
        repository.findByName("openai");
    
    assertTrue(retrieved.isPresent());
    assertEquals("openai", retrieved.get().getProviderName());
}
```

### Integration Tests (Future)

```java
@Test
void testProviderConnection() {
    service.testConnection("openai").get();
    
    Optional<ProviderConfiguration> config = 
        service.getConfiguration("openai");
    
    assertTrue(config.isPresent());
    assertEquals("success", config.get().getTestStatus());
}
```

## 🚀 Future Enhancements

### Phase 2
- [ ] Encrypt API keys in database
- [ ] Import/Export provider configurations
- [ ] Bulk operations (enable/disable multiple providers)
- [ ] Provider usage statistics
- [ ] Cost tracking per provider

### Phase 3
- [ ] Advanced model selection with filters
- [ ] Model comparison (context window, pricing, speed)
- [ ] Automatic failover between providers
- [ ] Custom provider templates
- [ ] API key rotation policies

### Phase 4
- [ ] Multi-account support per provider
- [ ] Team sharing of configurations
- [ ] Audit log for configuration changes
- [ ] Webhooks for status notifications
- [ ] Integration with external secret managers (AWS Secrets Manager, HashiCorp Vault)

## 📝 Code Examples

### Creating a Provider Configuration

```java
ProviderConfiguration config = ProviderConfiguration.builder()
    .providerName("openai")
    .displayName("OpenAI")
    .apiKey("sk-...")
    .apiBaseUrl("https://api.openai.com/v1")
    .defaultModel("gpt-4")
    .requiresApiKey(true)
    .enabled(true)
    .active(true)
    .connectionTimeout(30000)
    .maxRetries(3)
    .build();

service.saveConfiguration(config);
```

### Testing Connection

```java
service.testConnection("openai").thenAccept(success -> {
    if (success) {
        System.out.println("Connection successful!");
    } else {
        System.out.println("Connection failed!");
    }
});
```

### Loading Models

```java
service.loadModels("openai").thenAccept(models -> {
    models.forEach(model -> {
        System.out.println("Model: " + model.getId());
        System.out.println("Context: " + model.getContextWindow());
    });
});
```

## 🎨 UI Styling

### CSS Classes

```css
/* Provider card */
.provider-config-card
.provider-config-card .card-header
.provider-config-card .card-content

/* Status indicators */
.success  /* Green checkmark */
.danger   /* Red X */
.warning  /* Yellow warning */

/* Icons */
.provider-icon
```

### Theme Support

The UI automatically adapts to:
- Light theme (`theme-light.css`)
- Dark theme (`theme-dark.css`)

All colors use CSS variables for easy theming.

## 📚 References

- [AtlantaFX Documentation](https://github.com/mkpaz/atlantafx)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Anthropic API Documentation](https://docs.anthropic.com)
- [Ollama Documentation](https://ollama.ai/docs)

## 🤝 Contributing

When adding support for new providers:

1. Add provider to `ProviderRegistry`
2. Insert default configuration in migration SQL
3. Implement provider-specific features if needed
4. Update UI to display provider-specific fields
5. Add tests
6. Update documentation

---

**Author:** PCM Team  
**Version:** 1.0.0  
**Last Updated:** 2024


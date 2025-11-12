# PCM Desktop Integrations

PCM Desktop supports integration with various external systems to provide a comprehensive enterprise analysis platform.

## 🔌 Available Integrations

### 🤖 [API Integrations](api/)
Connect with AI services and external APIs for enhanced functionality.

- **[LLM Integration](api/llm-integration.md)** - OpenAI GPT, Anthropic Claude, Ollama
- **[API Integration Guide](api/api-guide.md)** - Complete guide for API integration
- **[Quick Reference](api/api-quick-reference.md)** - Quick API reference

**Features:**
- Multiple LLM providers support
- Streaming responses
- Function calling capabilities
- Automatic retry and error handling

### 🔐 [Single Sign-On (SSO)](sso/)
Integrate with enterprise authentication systems for seamless login.

- **[SSO Integration Guide](sso/sso-integration-guide.md)** - Complete SSO implementation
- **[SSO Quick Start](sso/sso-quick-start.md)** - Get started with SSO quickly

**Supported Sources:**
- Browser cookies (Chrome, Edge, Firefox)
- Browser localStorage
- Windows Registry
- Shared configuration files
- Manual token injection

### 🗄️ [Database](database/)
Connect to various database systems for data analysis and management.

- **[Database Setup](database/database-setup.md)** - Database configuration
- **[Migration Guide](database/migration-guide.md)** - Database schema management
- **[Quick Start](database/database-quick-start.md)** - Quick database setup

**Supported Databases:**
- SQLite (default)
- Oracle Database
- PostgreSQL (planned)
- MySQL (planned)

## 🛠️ Integration Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   External      │    │   PCM Desktop   │    │    Internal     │
│   Systems       │    │   Integration   │    │    Systems      │
│                 │    │     Layer       │    │                 │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ • LLM APIs      │◄──►│ • API Clients   │◄──►│ • Core Services │
│ • SSO Systems   │    │ • Auth Manager  │    │ • Data Layer    │
│ • Databases     │    │ • Config Mgmt   │    │ • UI Components │
│ • File Systems  │    │ • Error Handler │    │ • Business Logic│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Quick Setup

### 1. API Integration
```java
// Configure LLM service
LLMService llmService = new LLMService();
LLMProviderConfig config = LLMProviderConfig.builder()
    .provider(LLMProviderConfig.Provider.OPENAI)
    .token(System.getenv("OPENAI_API_KEY"))
    .model("gpt-4")
    .build();
llmService.initialize(config);
```

### 2. SSO Integration
```java
// Initialize SSO
SSOTokenManager ssoManager = SSOTokenManager.getInstance();
Optional<String> token = ssoManager.getToken("company-portal");
```

### 3. Database Integration
```java
// Connect to database
ConnectionManager.INSTANCE.initialize("jdbc:sqlite:pcm.db");
```

## 🔧 Configuration

### Environment Variables
```bash
# API Keys
export OPENAI_API_KEY="your-openai-key"
export ANTHROPIC_API_KEY="your-anthropic-key"

# Database
export PCM_DATABASE_URL="jdbc:sqlite:pcm.db"

# SSO
export PCM_SSO_ENABLED=true
export PCM_SSO_SERVICE="company-portal"
```

### Configuration Files
- `config/api-config.json` - API configuration
- `config/sso-config.json` - SSO configuration
- `config/database-config.json` - Database configuration

## 📊 Integration Status

| Integration | Status | Version | Last Updated |
|-------------|--------|---------|--------------|
| OpenAI API | ✅ Stable | 1.0.0 | 2024-11-12 |
| Anthropic API | ✅ Stable | 1.0.0 | 2024-11-12 |
| Ollama | ✅ Stable | 1.0.0 | 2024-11-12 |
| SSO (Cookies) | ✅ Stable | 1.0.0 | 2024-11-12 |
| SSO (Registry) | ✅ Stable | 1.0.0 | 2024-11-12 |
| SQLite | ✅ Stable | 1.0.0 | 2024-11-12 |
| Oracle DB | 🚧 In Progress | 0.9.0 | 2024-11-12 |

## 🧪 Testing Integrations

### Run Integration Tests
```bash
# Test all integrations
./scripts/test-integrations.sh

# Test specific integration
./scripts/test-api-integration.sh
./scripts/test-sso-integration.sh
./scripts/test-database-integration.sh
```

### Interactive Demos
```bash
# API integration demo
./scripts/run-api-demo.sh

# SSO integration demo  
./scripts/run-sso-demo.sh
```

## 🔒 Security Considerations

### API Security
- Store API keys in environment variables
- Use secure token rotation
- Implement rate limiting
- Enable audit logging

### SSO Security
- Encrypt cached tokens
- Implement token expiration
- Use secure storage locations
- Enable comprehensive audit trails

### Database Security
- Use connection encryption
- Implement access controls
- Regular backup procedures
- Monitor database access

## 📚 Best Practices

1. **Configuration Management**
   - Use environment variables for secrets
   - Version control configuration files
   - Document all configuration options

2. **Error Handling**
   - Implement retry logic with exponential backoff
   - Log all integration errors
   - Provide meaningful error messages

3. **Performance**
   - Cache frequently accessed data
   - Use connection pooling
   - Monitor integration performance

4. **Security**
   - Follow principle of least privilege
   - Regularly rotate credentials
   - Monitor for suspicious activity

## 🆘 Troubleshooting

Common integration issues and solutions:

- [API Connection Issues](../troubleshooting/common-issues.md#api-integration)
- [SSO Authentication Problems](../troubleshooting/common-issues.md#sso-integration)
- [Database Connection Issues](../troubleshooting/common-issues.md#database-integration)

## 🔄 Planned Integrations

- **Microsoft Graph API** - Office 365 integration
- **GitHub API** - Source code analysis
- **Jira API** - Project management integration
- **Confluence API** - Knowledge base integration
- **Slack API** - Team communication integration

---

For detailed integration guides, select the specific integration type from the menu above.
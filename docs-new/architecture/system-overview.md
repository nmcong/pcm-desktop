# 📘 PCM Desktop - System Overview & Architecture

## 🎯 Vision

**PCM (Project Code Management)** là một công cụ AI-powered được thiết kế để giúp các team phát triển và vận hành hệ thống phần mềm doanh nghiệp phức tạp có thể:

1. **Hiểu** - Nhanh chóng nắm bắt kiến trúc và logic nghiệp vụ của hệ thống
2. **Phân tích** - Sử dụng AI để phân tích code, database, và workflow
3. **Quản lý** - Tập trung hóa thông tin về toàn bộ hệ thống
4. **Truy vấn** - Đặt câu hỏi bằng ngôn ngữ tự nhiên và nhận câu trả lời chính xác

---

## 🏗️ System Components

### 1. **Subsystems & Projects**

Quản lý cấu trúc phân cấp của hệ thống:

```
Enterprise System
├── Subsystem A (e.g., Customer Management)
│   ├── Project 1 (e.g., Customer Registration)
│   ├── Project 2 (e.g., Customer Profile)
│   └── Project 3 (e.g., Customer Search)
├── Subsystem B (e.g., Order Management)
│   ├── Project 1 (e.g., Order Entry)
│   ├── Project 2 (e.g., Order Processing)
│   └── Project 3 (e.g., Order Tracking)
└── Subsystem C (e.g., Reporting)
    └── ...
```

**Thông tin được quản lý:**
- Tên và mô tả subsystem/project
- Owner/Team chịu trách nhiệm
- Trạng thái (Active, Maintenance, Deprecated)
- Dependencies giữa các subsystems
- Documentation links

### 2. **Screens & Forms**

Theo dõi tất cả màn hình/form trong hệ thống:

**Metadata của mỗi màn hình:**
- Screen ID và tên
- Subsystem/Project liên quan
- Screen type (List, Detail, Entry, Search, etc.)
- Mô tả nghiệp vụ
- **Events trên màn hình:**
  - Button clicks và actions
  - Data validation rules
  - Business logic flow
  - Navigation patterns

**Ví dụ:**
```
Screen: Customer Registration Form
├── Fields: Name, Email, Phone, Address
├── Events:
│   ├── Save Button → Validate → Call CustomerService.save()
│   ├── Cancel Button → Navigate back to Customer List
│   └── Email Field → Real-time validation
└── Business Rules:
    ├── Email must be unique
    ├── Phone format validation
    └── Address required for premium customers
```

### 3. **Database Objects**

Quản lý toàn bộ database schema và relationships:

**Tables & Views:**
- Table structure và columns
- Primary keys, foreign keys
- Indexes và constraints
- Data types và validation rules
- Business meaning của từng field

**Stored Procedures & Functions:**
- Input/output parameters
- Business logic implementation
- Dependencies trên tables
- Performance characteristics

**Triggers & Jobs:**
- Trigger conditions và actions
- Scheduled job definitions
- Data transformation logic

### 4. **Batch Jobs & Workflows**

Theo dõi tất cả các batch processing jobs:

**Job Definitions:**
- Job name và description
- Schedule (daily, weekly, monthly)
- Input data sources
- Output destinations
- Dependencies giữa các jobs

**Workflow Management:**
- Job execution status
- Error handling và retry logic
- Data volume metrics
- Performance monitoring

### 5. **Knowledge Base**

Lưu trữ institutional knowledge:

**Documentation:**
- Technical specifications
- Business requirements
- Architecture decisions
- Troubleshooting guides

**AI-Enhanced Search:**
- Natural language queries
- Context-aware answers
- Related information suggestions
- Knowledge graph connections

---

## 🤖 AI Integration Architecture

### 1. **Multi-LLM Support**

```
User Query → LLM Router → Provider Selection
                ↓
┌─────────────────────────────────────────────────────────┐
│  LLM Providers                                          │
├─────────────────┬─────────────────┬─────────────────────┤
│   OpenAI GPT    │ Anthropic Claude│     Local LLM       │
│   • GPT-4       │   • Claude-3    │   • Ollama          │
│   • GPT-3.5     │   • Claude-2    │   • Custom Models   │
└─────────────────┴─────────────────┴─────────────────────┘
                ↓
         Context Enhancement
                ↓
           Response Generation
```

### 2. **Context-Aware AI**

AI sử dụng context từ:
- Current project/subsystem
- Related database objects
- Historical queries
- User permissions
- System documentation

### 3. **Intelligent Query Processing**

```
Natural Language Query
         ↓
   Intent Recognition
         ↓
   Context Retrieval
         ↓
   Query Enhancement
         ↓
   LLM Processing
         ↓
   Response Formatting
         ↓
   User Interface
```

---

## 🏛️ Technical Architecture

### 1. **Layered Architecture**

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer                              │
│  JavaFX + AtlantaFX + Custom Components               │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                Service Layer                            │
│  Business Logic + AI Integration + Validation          │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                Repository Layer                         │
│  Data Access + Caching + Transaction Management        │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│                   Data Layer                            │
│     SQLite + File System + External APIs               │
└─────────────────────────────────────────────────────────┘
```

### 2. **Component Design**

**UI Components:**
```
MainWindow
├── NavigationPanel
│   ├── SubsystemTree
│   ├── ProjectTree
│   └── QuickSearch
├── ContentArea
│   ├── TabManager
│   ├── ScreenEditor
│   ├── DatabaseViewer
│   └── AIChat
└── StatusBar
    ├── ConnectionStatus
    ├── AIStatus
    └── SystemInfo
```

**Core Services:**
```
ServiceLayer
├── SubsystemService
├── ProjectService
├── ScreenService
├── DatabaseService
├── BatchJobService
├── KnowledgeService
├── AIService
└── ConfigurationService
```

### 3. **Data Management**

**Repository Pattern:**
```java
interface ProjectRepository {
    List<Project> findAll();
    Optional<Project> findById(Long id);
    List<Project> findBySubsystem(Long subsystemId);
    Long save(Project project);
    boolean update(Project project);
    boolean delete(Long id);
}
```

**Specification Pattern for Complex Queries:**
```java
Specification<Project> spec = ProjectSpecifications
    .hasStatus("ACTIVE")
    .and(ProjectSpecifications.belongsToSubsystem("CUSTOMER_MGMT"))
    .and(ProjectSpecifications.lastUpdatedAfter(LocalDate.now().minusMonths(3)));
    
List<Project> projects = projectRepository.findAll(spec);
```

---

## 🔄 Data Flow Architecture

### 1. **User Interaction Flow**

```
User Action → UI Controller → Service Layer → Repository → Database
     ↓              ↓              ↓             ↓          ↓
UI Update ← Response ← Business Logic ← Data Access ← Query Result
```

### 2. **AI Query Flow**

```
Natural Language Input
         ↓
    UI Component (AIChat)
         ↓
    AIService.processQuery()
         ↓
    Context Collection
    ├── Current Project Context
    ├── Related Database Objects
    ├── Recent User Actions
    └── System Documentation
         ↓
    LLM API Call
         ↓
    Response Processing
         ↓
    UI Display with Actions
```

### 3. **Database Operation Flow**

```
UI Request → Validation → Service Logic → Repository → DAO → SQLite
    ↑             ↑            ↑            ↑        ↑       ↑
UI Update ← Mapping ← Business Rules ← Entity ← Result ← Raw Data
```

---

## 🔐 Security Architecture

### 1. **Data Protection**
- Local SQLite database encryption
- Secure API key management
- User session management
- Audit trail logging

### 2. **AI Security**
- API key rotation
- Request/response sanitization
- Context filtering
- Rate limiting

### 3. **Access Control**
- Role-based permissions
- Feature-level access control
- Data-level filtering
- Operation auditing

---

## 📊 Performance Architecture

### 1. **Caching Strategy**
```
Memory Cache (UI Components)
     ↓
Application Cache (Service Layer)
     ↓
Database Cache (Repository Layer)
     ↓
Disk Storage (SQLite)
```

### 2. **Asynchronous Operations**
- Background data loading
- Non-blocking AI queries
- Parallel database operations
- Progressive UI updates

### 3. **Resource Management**
- Connection pooling
- Memory optimization
- Lazy loading
- Garbage collection tuning

---

## 🔄 Extensibility Architecture

### 1. **Plugin System**
```
Core PCM Desktop
     ↓
Plugin Manager
     ↓
┌──────────────┬──────────────┬──────────────┐
│   Database   │     AI       │    Export    │
│   Plugins    │   Plugins    │   Plugins    │
│              │              │              │
│ • Oracle     │ • Custom LLM │ • PDF Export │
│ • PostgreSQL │ • Local AI   │ • Excel      │
│ • MySQL      │ • Embeddings │ • Reports    │
└──────────────┴──────────────┴──────────────┘
```

### 2. **Configuration System**
- Environment-specific configs
- User preferences
- Plugin configurations
- Runtime settings

### 3. **Integration Points**
- REST API endpoints
- Event system
- Configuration hooks
- Custom UI components

---

## 🎯 Use Case Scenarios

### 1. **New Developer Onboarding**
```
1. New developer joins team
2. Opens PCM Desktop
3. Browses subsystem hierarchy
4. Asks AI: "How does customer registration work?"
5. AI provides:
   ├── Related screens and workflows
   ├── Database tables and relationships
   ├── Key business rules
   └── Code examples and documentation
```

### 2. **Bug Investigation**
```
1. Bug report received
2. Developer searches for related screens
3. Examines database objects and triggers
4. Asks AI: "What could cause duplicate customer emails?"
5. AI suggests:
   ├── Missing unique constraints
   ├── Race conditions in registration
   ├── Data import issues
   └── Related batch job problems
```

### 3. **System Documentation**
```
1. Need to document new feature
2. AI analyzes code and database changes
3. Generates documentation including:
   ├── Architecture diagrams
   ├── Database schema changes
   ├── API documentation
   └── User guide sections
```

---

## 🔮 Future Roadmap

### Phase 1: Core Foundation ✅
- Basic UI framework
- SQLite database
- Simple AI integration
- Project/screen management

### Phase 2: AI Enhancement 🚧
- Advanced LLM integration
- Context-aware responses
- Knowledge base management
- Batch job monitoring

### Phase 3: Advanced Features 📋
- Oracle database support
- Plugin system
- Advanced analytics
- Team collaboration features

### Phase 4: Enterprise Features 📋
- Multi-database support
- Advanced security
- Performance optimization
- Enterprise integration

---

This architecture provides a solid foundation for building a comprehensive system analysis and management tool that can grow with enterprise needs while maintaining simplicity and usability.

*Last updated: November 12, 2024*
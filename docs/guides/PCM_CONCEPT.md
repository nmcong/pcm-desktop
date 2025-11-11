# 📘 PCM Desktop - Concept & Architecture

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
  - Button clicks (Save, Update, Delete, Search, etc.)
  - Form submissions
  - Field validations
  - Data loading events
  - Navigation events
- **Source code mapping:**
  - Frontend code files (Java, JSP, HTML, JavaScript, etc.)
  - Controller/Action classes
  - Service classes được gọi
  - DAO/Repository classes
  - Configuration files
- UI mockups/screenshots
- User roles có quyền truy cập

**Use Case Example:**
```
Screen: Customer Registration Form
Events:
  - onLoad: Validate user permission, load countries list
  - onSaveClick: Validate form, call CustomerService.register()
  - onCancelClick: Navigate back to customer list
  - onFieldChange: Real-time validation

Source Files:
  - CustomerRegistrationController.java
  - CustomerService.java
  - CustomerRepository.java
  - customer-registration.jsp
  - customer-validation.js
```

### 3. **Database Objects (Oracle)**

Quản lý toàn bộ Oracle database objects:

**Object Types:**
- **Tables** - Structure, columns, constraints, indexes, partitions
- **Views** - Definition, dependencies
- **Stored Procedures** - Code, parameters, logic
- **Functions** - Code, return type, usage
- **Packages** - Package spec & body
- **Triggers** - Trigger timing, event, logic
- **Sequences** - Current value, increment
- **Synonyms** - Target objects
- **Types** - Object types, collections
- **Materialized Views** - Refresh schedule, query

**Metadata cho mỗi object:**
- Object name và schema
- Creation/modification date
- Owner và permissions
- Source code (for procedures, functions, packages, triggers)
- Dependencies (tables/views used, calls to other objects)
- Business description
- Usage statistics
- Related screens/features

**Relationship Tracking:**
```
Table: CUSTOMERS
├── Referenced by:
│   ├── View: V_CUSTOMER_SUMMARY
│   ├── Procedure: P_UPDATE_CUSTOMER
│   └── Trigger: TRG_CUSTOMERS_AUDIT
├── Foreign Keys:
│   ├── FK_CUSTOMER_COUNTRY → COUNTRIES
│   └── FK_CUSTOMER_TYPE → CUSTOMER_TYPES
├── Used in Screens:
│   ├── Customer Registration
│   ├── Customer Profile
│   └── Customer Search
└── Modified by Batch Jobs:
    └── BATCH_CUSTOMER_IMPORT
```

### 4. **Batch Jobs**

Quản lý thông tin về batch jobs (không phải instances, mà là metadata):

**Job Configuration:**
- Job ID và tên
- Mô tả nghiệp vụ
- **Schedule Information:**
  - Cron expression hoặc schedule pattern
  - Run frequency (Daily, Weekly, Monthly, etc.)
  - Preferred execution time
  - Time zone
- **Technical Details:**
  - Source code files
  - Main class/entry point
  - Parameters và configuration
  - Database connections used
  - Tables read/written
  - File I/O operations
- **Dependencies:**
  - Predecessor jobs (must run before)
  - Successor jobs (run after this)
  - External system dependencies
- **Error Handling:**
  - Retry logic
  - Alert/notification rules
  - Fallback procedures
- **Performance:**
  - Expected runtime
  - Resource requirements
  - Historical performance metrics

**Use Case Example:**
```
Job: Daily Customer Data Import
Schedule: Every day at 2:00 AM (Asia/Tokyo)
Source Code: com.example.batch.CustomerImportJob.java
Database: PROD_DB (READ/WRITE)
Tables:
  - Read: STAGING_CUSTOMERS
  - Write: CUSTOMERS, CUSTOMER_AUDIT
Dependencies:
  - After: File Arrival Check Job
  - Before: Customer Validation Job
```

### 5. **Workflows**

Quản lý và visualize quy trình nghiệp vụ:

**Workflow Components:**
- **Process Name** - Tên workflow
- **Steps** - Các bước trong quy trình
- **Decision Points** - Điều kiện rẽ nhánh
- **Actors** - Users/Roles tham gia
- **Systems** - Subsystems liên quan
- **Data Flow** - Dữ liệu truyền giữa các bước

**Workflow Types:**
- Manual workflows (user-driven)
- Automated workflows (system-driven)
- Hybrid workflows
- Approval workflows

**Example:**
```
Workflow: Customer Order Process
1. Customer submits order (Order Entry Screen)
2. System validates inventory (OrderValidationService)
3. If stock available:
   a. Reserve inventory (InventoryService)
   b. Calculate pricing (PricingService)
   c. Create order record (OrderService)
4. Else:
   a. Create backorder
   b. Notify customer
5. Send to payment gateway
6. Update order status
7. Trigger fulfillment (Batch Job: Order Fulfillment)
```

### 6. **Knowledge Base**

Tập trung hóa tài liệu và kiến thức về hệ thống:

**Document Categories:**
- **Business Rules** - Nghiệp vụ rules và logic
- **Technical Specifications** - Spec kỹ thuật
- **Architecture Documents** - Kiến trúc hệ thống
- **API Documentation** - API specs và examples
- **Database Schema** - ER diagrams, data dictionary
- **Deployment Guides** - Hướng dẫn triển khai
- **Troubleshooting** - Common issues và solutions
- **Best Practices** - Coding standards, design patterns
- **Release Notes** - Version history, changes
- **Training Materials** - User guides, tutorials

**Features:**
- Full-text search
- Version control
- Tags và categories
- Related documents linking
- AI-powered Q&A

---

## 🤖 AI-Powered Query Interface

### Natural Language Processing

Người dùng có thể đặt câu hỏi bằng ngôn ngữ tự nhiên:

**Example Queries:**

```
Q: "Màn hình customer registration gọi những stored procedures nào?"
A: PCM phân tích source code và database dependencies, trả về:
   - P_VALIDATE_CUSTOMER
   - P_INSERT_CUSTOMER
   - P_LOG_AUDIT_TRAIL

Q: "Batch job nào chạy lúc 2 giờ sáng?"
A: - Daily Customer Data Import (2:00 AM)
   - Order Reconciliation Job (2:15 AM)
   - Inventory Sync Job (2:30 AM)

Q: "Table CUSTOMERS được sử dụng ở đâu?"
A: - Screens: Customer Registration, Customer Profile, Customer Search
   - Procedures: P_UPDATE_CUSTOMER, P_DELETE_CUSTOMER
   - Batch Jobs: Customer Import, Customer Export
   - Triggers: TRG_CUSTOMERS_AUDIT

Q: "Workflow của order processing như thế nào?"
A: [Hiển thị workflow diagram và mô tả các bước]

Q: "Source code của màn hình order entry nằm ở đâu?"
A: - Controller: OrderEntryController.java (line 45-320)
   - Service: OrderService.java (line 120-450)
   - View: order-entry.jsp
   - JavaScript: order-validation.js

Q: "Những màn hình nào gọi function F_CALCULATE_DISCOUNT?"
A: - Order Entry Screen
   - Quote Generation Screen
   - Promotion Management Screen
```

### LLM Integration

**Supported Models:**
- OpenAI GPT-4
- Anthropic Claude
- Google Gemini
- Azure OpenAI
- Custom/Local LLMs

**AI Capabilities:**
- Code analysis và explanation
- Dependency tracing
- Impact analysis
- Code suggestions
- Documentation generation
- Query optimization

---

## 🗄️ Data Model

### Core Entities

```sql
-- Subsystems & Projects
SUBSYSTEMS (id, name, description, owner, status, created_at)
PROJECTS (id, subsystem_id, name, description, status, created_at)

-- Screens & Events
SCREENS (id, project_id, screen_id, name, type, description, created_at)
SCREEN_EVENTS (id, screen_id, event_name, event_type, description, handler)
SCREEN_SOURCE_FILES (screen_id, file_path, file_type, class_name)

-- Database Objects
DB_OBJECTS (id, schema, object_type, object_name, source_code, created_at)
DB_DEPENDENCIES (object_id, depends_on_object_id, dependency_type)
SCREEN_DB_MAPPINGS (screen_id, db_object_id, access_type)

-- Batch Jobs
BATCH_JOBS (id, job_name, description, schedule_cron, source_code_path, created_at)
BATCH_DB_CONNECTIONS (job_id, db_name, access_type)
BATCH_DEPENDENCIES (job_id, depends_on_job_id)

-- Workflows
WORKFLOWS (id, name, description, workflow_type, created_at)
WORKFLOW_STEPS (id, workflow_id, step_order, step_name, description)
WORKFLOW_ACTORS (workflow_id, step_id, actor_role)

-- Knowledge Base
KNOWLEDGE_ENTRIES (id, title, content, category, tags, created_at)
KNOWLEDGE_LINKS (entry_id, linked_entity_type, linked_entity_id)
```

---

## 🔄 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │Dashboard │ │Subsystems│ │ Screens  │ │   DB     │ ...   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ LLM Service │ │ Code        │ │   Query     │           │
│  │             │ │ Analyzer    │ │   Service   │   ...     │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Screen   │ │ DBObject │ │BatchJob  │ │Workflow  │ ...   │
│  │ Entity   │ │ Entity   │ │ Entity   │ │ Entity   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │  SQLite     │ │   Oracle    │ │   LLM API   │           │
│  │(Metadata DB)│ │(Target DB)  │ │ Integration │   ...     │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Use Cases

### For System Analysts
- **Understand system architecture** - "Show me all subsystems and their relationships"
- **Analyze business flows** - "What is the workflow for order processing?"
- **Find functionality** - "Which screens handle customer registration?"

### For Developers
- **Code navigation** - "Show me all code files for order entry screen"
- **Dependency analysis** - "What will be impacted if I change table CUSTOMERS?"
- **Debug assistance** - "Which batch job updates the INVENTORY table?"

### For DBAs
- **Schema exploration** - "List all tables in CUSTOMER schema"
- **Dependency tracking** - "What objects depend on table ORDERS?"
- **Performance analysis** - "Which procedures take longest to execute?"

### For Project Managers
- **Complexity assessment** - "How many screens are in Customer Management subsystem?"
- **Resource planning** - "What components need refactoring?"
- **Impact analysis** - "What will be affected by this new requirement?"

---

## 🚀 Future Enhancements

### Phase 2
- Real-time code parsing và auto-sync
- Git integration để track changes
- Visual workflow designer
- Advanced code metrics và quality analysis

### Phase 3
- Multi-database support (PostgreSQL, MySQL, SQL Server)
- API management
- Test coverage tracking
- CI/CD integration

### Phase 4
- Collaborative features (comments, annotations)
- Version comparison
- AI-powered code refactoring suggestions
- Automated documentation generation

---

## 📊 Benefits

### Time Savings
- **90% faster** system understanding for new team members
- **70% reduction** in time spent searching for code
- **50% faster** impact analysis for changes

### Quality Improvements
- Better code documentation
- Reduced knowledge silos
- Improved system maintainability

### Risk Reduction
- Clear dependency tracking
- Impact analysis before changes
- Centralized knowledge retention

---

**PCM Desktop - Making Enterprise Software Manageable** 🚀


# 🎨 AI Chat Message Flow - Visual Diagrams

**Interactive visual diagrams for message processing flow**

**Version**: 1.0.0  
**Last Updated**: November 10, 2025

---

## 📊 Complete Message Flow Diagram

```mermaid
sequenceDiagram
    participant User
    participant AIPanel
    participant ConversationMgr
    participant PlanningService
    participant PromptService
    participant ProviderRegistry
    participant Provider
    participant FunctionService
    participant DatabaseMgr

    User->>AIPanel: Type & Submit Message

    Note over AIPanel: Phase 1: Input Processing
    AIPanel->>AIPanel: Validate Input
    AIPanel->>ConversationMgr: Create/Get Conversation
    ConversationMgr-->>AIPanel: Conversation ID
    AIPanel->>AIPanel: Display User Message
    AIPanel->>ConversationMgr: Persist Message
    ConversationMgr->>ConversationMgr: Save to IndexedDB

    Note over AIPanel,PlanningService: Phase 2: Planning & Intent Detection
    AIPanel->>PlanningService: detectIntent(message)
    PlanningService-->>AIPanel: Intent Result
    AIPanel->>PlanningService: needsPlanning(message, intent)

    alt Planning Needed
        PlanningService-->>AIPanel: true
        AIPanel->>PlanningService: generatePlan(message, intent)
        PlanningService-->>AIPanel: Execution Plan
        AIPanel->>AIPanel: Display Plan to User
    else No Planning
        PlanningService-->>AIPanel: false
    end

    AIPanel->>PromptService: buildSystemPrompt(options)
    PromptService->>DatabaseMgr: getSystemStatistics()
    DatabaseMgr-->>PromptService: Statistics
    PromptService-->>AIPanel: Enhanced System Prompt

    Note over AIPanel,Provider: Phase 3: Provider Communication
    AIPanel->>ProviderRegistry: getActive()
    ProviderRegistry-->>AIPanel: Active Provider
    AIPanel->>Provider: isAvailable()
    Provider-->>AIPanel: Health Check OK

    AIPanel->>FunctionService: getAvailableFunctions()
    FunctionService-->>AIPanel: Function Definitions
    AIPanel->>AIPanel: Format Tools for Provider

    Note over AIPanel,Provider: Phase 4: Function Calling Loop
    loop Tool Iterations (max 10)
        AIPanel->>Provider: chat(messages, {tools, model, ...})
        Provider->>Provider: Call AI API
        Provider-->>AIPanel: Response

        alt Has Tool Calls
            AIPanel->>AIPanel: Add Assistant Message with Tool Calls

            loop For Each Tool Call
                AIPanel->>FunctionService: executeFunction(name, args)
                FunctionService->>FunctionService: Validate Function Exists
                FunctionService->>DatabaseMgr: Execute Function
                DatabaseMgr-->>FunctionService: Function Result
                FunctionService-->>AIPanel: Execution Result

                opt Planning Mode
                    AIPanel->>PlanningService: validateStepResult(step, result)
                    PlanningService-->>AIPanel: Validation Result
                end

                AIPanel->>AIPanel: Add Tool Result to Messages
            end

            Note over AIPanel: Continue Loop for Next Iteration
        else Final Response
            AIPanel->>AIPanel: Display Final Response
            AIPanel->>ConversationMgr: Persist Response
            Note over AIPanel: Break Loop
        end
    end

    Note over AIPanel,User: Phase 5: Response Complete
    AIPanel->>User: Show Final Answer
```

---

## 🔀 Simple Flow (No Planning)

```mermaid
flowchart TD
    A[User Types Message] --> B[Validate Input]
    B --> C{Is Processing?}
    C -->|Yes| Z[Ignore]
    C -->|No| D[Create/Get Conversation]
    D --> E[Display User Message]
    E --> F[Save to IndexedDB]
    F --> G[Get Active Provider]
    G --> H{Provider Available?}
    H -->|No| ERROR[Show Error]
    H -->|Yes| I{Function Calling Enabled?}

    I -->|No| J[Simple Chat]
    J --> K[Call Provider API]
    K --> L[Display Response]
    L --> M[Save Response]
    M --> END[Done]

    I -->|Yes| N[Get Function Definitions]
    N --> O[Format Tools]
    O --> P[Call Provider with Tools]
    P --> Q{Has Tool Calls?}

    Q -->|No| L
    Q -->|Yes| R[Execute Functions]
    R --> S[Add Results to Messages]
    S --> P

    ERROR --> END

    style A fill:#e1f5ff
    style END fill:#c8e6c9
    style ERROR fill:#ffcdd2
    style L fill:#fff9c4
```

---

## 🧠 Planning Flow (Complex Queries)

```mermaid
flowchart TD
    START[User Message] --> INTENT[Detect Intent]
    INTENT --> CHECK{Needs Planning?}

    CHECK -->|No| SIMPLE[Direct Execution]
    SIMPLE --> PROVIDER[Call Provider]

    CHECK -->|Yes| PLAN[Generate Execution Plan]
    PLAN --> SHOW[Display Plan to User]
    SHOW --> BUILD[Build Enhanced Prompt]
    BUILD --> INJECT[Inject Plan into System Prompt]
    INJECT --> PROVIDER

    PROVIDER --> LOOP{Has Tool Calls?}

    LOOP -->|Yes| EXEC[Execute Tool]
    EXEC --> VALIDATE{Planning Mode?}

    VALIDATE -->|Yes| CHECK_STEP[Validate Step Result]
    CHECK_STEP --> STEP_OK{Step Valid?}
    STEP_OK -->|No| WARN[Log Warning]
    STEP_OK -->|Yes| NEXT[Next Step]
    WARN --> NEXT

    VALIDATE -->|No| NEXT
    NEXT --> ADD[Add Result to Messages]
    ADD --> LOOP

    LOOP -->|No| FINAL[Display Final Response]
    FINAL --> SAVE[Save to History]
    SAVE --> END[Done]

    style START fill:#e3f2fd
    style PLAN fill:#fff3e0
    style CHECK_STEP fill:#f3e5f5
    style FINAL fill:#e8f5e9
    style END fill:#c8e6c9
```

---

## 🔄 Function Calling Iteration Loop

```mermaid
flowchart LR
    subgraph "Iteration 1"
        A1[Provider Call] --> B1{Tool Calls?}
        B1 -->|Yes| C1[Execute Tools]
        C1 --> D1[Add Results]
    end

    subgraph "Iteration 2"
        A2[Provider Call] --> B2{Tool Calls?}
        B2 -->|Yes| C2[Execute Tools]
        C2 --> D2[Add Results]
    end

    subgraph "Iteration N"
        A3[Provider Call] --> B3{Tool Calls?}
        B3 -->|No| C3[Final Response]
    end

    D1 --> A2
    D2 --> A3

    C3 --> END[Display & Save]

    style A1 fill:#e1f5ff
    style A2 fill:#e1f5ff
    style A3 fill:#e1f5ff
    style C3 fill:#c8e6c9
    style END fill:#4caf50,color:#fff
```

---

## 🎯 Intent Detection Flow

```mermaid
flowchart TD
    MSG[User Message] --> ANALYZE[Analyze Message]

    ANALYZE --> KEYWORDS[Check Keywords]
    ANALYZE --> ENTITIES[Extract Entities]
    ANALYZE --> PATTERNS[Match Patterns]

    KEYWORDS --> CATEGORY{Determine Category}
    ENTITIES --> CATEGORY
    PATTERNS --> CATEGORY

    CATEGORY -->|"analyze", "impact"| ANALYSIS[ANALYSIS]
    CATEGORY -->|"compare", "vs"| COMPARISON[COMPARISON]
    CATEGORY -->|"find", "search"| SEARCH[SEARCH]
    CATEGORY -->|"get", "show"| INFO[INFORMATION]
    CATEGORY -->|"why", "error"| TROUBLE[TROUBLESHOOTING]

    ANALYSIS --> RESULT[Intent Result]
    COMPARISON --> RESULT
    SEARCH --> RESULT
    INFO --> RESULT
    TROUBLE --> RESULT

    RESULT --> CONFIDENCE[Calculate Confidence]
    CONFIDENCE --> TOOLS[Suggest Tools]
    TOOLS --> OUTPUT[Output Intent]

    style MSG fill:#e3f2fd
    style ANALYSIS fill:#ffccbc
    style COMPARISON fill:#ffccbc
    style SEARCH fill:#c5e1a5
    style INFO fill:#c5e1a5
    style TROUBLE fill:#ffccbc
    style OUTPUT fill:#fff9c4
```

---

## 🛠️ Function Execution Flow

```mermaid
stateDiagram-v2
    [*] --> Received: Tool Call from AI

    Received --> Validating: Validate Function Name
    Validating --> NotFound: Function Not Found
    Validating --> Parsing: Function Found

    NotFound --> Error: List Available Functions

    Parsing --> ParseError: Parse Arguments Failed
    Parsing --> Executing: Arguments Parsed

    ParseError --> Executing: Use Empty Object

    Executing --> CheckResult: Function Executed

    CheckResult --> Success: Result OK
    CheckResult --> Failed: Execution Error

    Success --> Logging: Log Success
    Failed --> Logging: Log Error

    Logging --> Recording: Record in History
    Recording --> [*]: Return Result

    Error --> [*]: Throw Error
```

---

## 📦 Provider Architecture

```mermaid
classDiagram
    class BaseProvider {
        +String id
        +String name
        +Object capabilities
        +chat(messages, options)
        +isAvailable()
        +getModels()
    }

    class OpenAIProvider {
        +NativeFunctionCallingAdapter adapter
        +chat(messages, options)
        +streamChat(messages, options)
    }

    class ClaudeProvider {
        +NativeFunctionCallingAdapter adapter
        +chat(messages, options)
        +streamChat(messages, options)
    }

    class ViByteProvider {
        +TextBasedFunctionCallingAdapter adapter
        +chat(messages, options)
        +messagesToPrompt(messages)
    }

    class ProviderRegistry {
        +Map providers
        +String activeProviderId
        +register(provider)
        +getActive()
        +setActive(id)
    }

    BaseProvider <|-- OpenAIProvider
    BaseProvider <|-- ClaudeProvider
    BaseProvider <|-- ViByteProvider

    ProviderRegistry --> BaseProvider: manages
```

---

## 🗂️ Component Relationships

```mermaid
graph TB
    subgraph "UI Layer"
        AIPanel[AIPanel]
        ChatView[ChatView]
        ModelSelector[ModelSelector]
    end

    subgraph "State Management"
        ConvMgr[ConversationManager]
        HistMgr[HistoryManager]
    end

    subgraph "AI Services"
        ProvReg[ProviderRegistry]
        FuncSvc[FunctionCallingService]
        PlanSvc[PlanningService]
        IntentSvc[IntentDetectionService]
        PromptSvc[PromptService]
    end

    subgraph "Providers"
        OpenAI[OpenAI]
        Claude[Claude]
        ViByte[ViByte]
    end

    subgraph "Data Layer"
        DBMgr[DatabaseManager]
        IndexedDB[(IndexedDB)]
    end

    AIPanel --> ChatView
    AIPanel --> ModelSelector
    AIPanel --> ConvMgr
    AIPanel --> ProvReg
    AIPanel --> FuncSvc
    AIPanel --> PlanSvc
    AIPanel --> IntentSvc

    ConvMgr --> HistMgr
    HistMgr --> IndexedDB

    ProvReg --> OpenAI
    ProvReg --> Claude
    ProvReg --> ViByte

    FuncSvc --> DBMgr
    PlanSvc --> IntentSvc
    PromptSvc --> DBMgr

    DBMgr --> IndexedDB

    style AIPanel fill:#e3f2fd
    style ConvMgr fill:#f3e5f5
    style ProvReg fill:#fff3e0
    style DBMgr fill:#e8f5e9
```

---

## 🔐 Error Handling Flow

```mermaid
flowchart TD
    START[Operation Start] --> TRY{Try Operation}

    TRY --> SUCCESS[Success]
    TRY --> ERROR{Error Type?}

    ERROR -->|No Provider| E1[Show 'No Provider']
    ERROR -->|Provider Unavailable| E2[Show 'Provider Unavailable']
    ERROR -->|API Error| E3[Parse Error Response]
    ERROR -->|Function Not Found| E4[List Available Functions]
    ERROR -->|Function Error| E5[Add Error to Messages]
    ERROR -->|Parse Error| E6[Use Defaults]
    ERROR -->|Network Timeout| E7[Show Timeout Error]

    E1 --> DISPLAY[Display Error to User]
    E2 --> DISPLAY
    E3 --> DISPLAY
    E4 --> THROW[Throw Error]
    E5 --> CONTINUE[Continue Processing]
    E6 --> CONTINUE
    E7 --> DISPLAY

    SUCCESS --> COMPLETE[Operation Complete]
    DISPLAY --> CLEANUP[Cleanup State]
    THROW --> CLEANUP
    CONTINUE --> RETRY[Retry/Continue]

    CLEANUP --> END[End]
    COMPLETE --> END
    RETRY --> TRY

    style START fill:#e3f2fd
    style SUCCESS fill:#c8e6c9
    style ERROR fill:#ffccbc
    style END fill:#90caf9
```

---

## 📊 Data Flow Diagram

```mermaid
flowchart LR
    subgraph "User Input"
        UI[User Types]
    end

    subgraph "Processing"
        VALIDATE[Validate]
        INTENT[Detect Intent]
        PLAN[Generate Plan]
        PROMPT[Build Prompt]
    end

    subgraph "AI Communication"
        FORMAT[Format Request]
        API[Call AI API]
        PARSE[Parse Response]
    end

    subgraph "Tool Execution"
        CHECK[Check Tool Calls]
        EXEC[Execute Functions]
        RESULT[Get Results]
    end

    subgraph "Storage"
        MEMORY[In-Memory State]
        DB[(IndexedDB)]
    end

    subgraph "Display"
        RENDER[Render Messages]
        SHOW[Show to User]
    end

    UI --> VALIDATE
    VALIDATE --> INTENT
    INTENT --> PLAN
    PLAN --> PROMPT
    PROMPT --> FORMAT
    FORMAT --> API
    API --> PARSE
    PARSE --> CHECK
    CHECK --> EXEC
    EXEC --> RESULT
    RESULT --> API
    PARSE --> RENDER
    RENDER --> SHOW

    VALIDATE --> MEMORY
    RESULT --> MEMORY
    RENDER --> MEMORY

    VALIDATE --> DB
    RESULT --> DB

    style UI fill:#e1f5ff
    style API fill:#fff9c4
    style EXEC fill:#ffe0b2
    style DB fill:#c8e6c9
    style SHOW fill:#90caf9,color:#fff
```

---

## 🎭 Lifecycle Diagram

```mermaid
timeline
    title AI Chat Message Lifecycle
    section User Action
        User Types : Input validation
                   : Create conversation
        User Submits : Display message
                     : Save to IndexedDB
    section AI Processing
        Intent Detection : Analyze query
                        : Detect category
        Planning : Generate plan
                 : Show to user
        Prompt Building : Add statistics
                       : Add examples
                       : Add plan
    section Provider
        Provider Call : Select provider
                     : Health check
                     : Format tools
        API Request : Send to AI
                   : Wait for response
    section Tool Execution
        Parse Tool Calls : Extract functions
                        : Parse arguments
        Execute Functions : Validate function
                         : Call handler
                         : Return result
        Validate Results : Check success
                        : Verify data
    section Completion
        Final Response : Display to user
                      : Save to history
        Cleanup : Reset state
                : Enable input
```

---

## 🔄 State Machine

```mermaid
stateDiagram-v2
    [*] --> Idle: Component Initialized

    Idle --> Processing: User Submits Message

    Processing --> IntentDetection: Message Validated
    IntentDetection --> PlanGeneration: Intent Detected
    IntentDetection --> PromptBuilding: No Planning Needed

    PlanGeneration --> PromptBuilding: Plan Generated

    PromptBuilding --> ProviderSelection: Prompt Ready

    ProviderSelection --> ProviderError: No Provider/Unavailable
    ProviderSelection --> APICall: Provider Ready

    APICall --> ParsingResponse: Response Received
    APICall --> APIError: Request Failed

    ParsingResponse --> ToolExecution: Has Tool Calls
    ParsingResponse --> DisplayingResponse: No Tool Calls

    ToolExecution --> ParsingResponse: Tools Executed (Continue Loop)
    ToolExecution --> ToolError: Execution Failed

    DisplayingResponse --> SavingHistory: Response Displayed

    SavingHistory --> Idle: Complete

    ProviderError --> Idle: Error Displayed
    APIError --> Idle: Error Displayed
    ToolError --> ParsingResponse: Error Added to Messages

    note right of Processing
        isProcessing = true
        showTypingIndicator()
    end note

    note right of Idle
        isProcessing = false
        Input enabled
    end note
```

---

## 📝 Summary

### Diagram Types

| Diagram              | Purpose                              | Best For                          |
|----------------------|--------------------------------------|-----------------------------------|
| **Sequence Diagram** | Show interactions between components | Understanding communication flow  |
| **Flowchart**        | Show decision logic                  | Understanding control flow        |
| **State Machine**    | Show state transitions               | Understanding component lifecycle |
| **Class Diagram**    | Show relationships                   | Understanding architecture        |
| **Timeline**         | Show chronological flow              | Understanding process steps       |

### Key Takeaways

1. **Message flow** involves 5 main phases
2. **Function calling** uses iterative loops (max 10)
3. **Planning** enhances complex queries
4. **Error handling** occurs at multiple levels
5. **State management** is critical for conversation continuity

---

## 🔗 Related Documents

- [MESSAGE_FLOW_ARCHITECTURE.md](./MESSAGE_FLOW_ARCHITECTURE.md) - Detailed text documentation
- [AI_PLANNING_STRATEGY_GUIDE.md](./AI_PLANNING_STRATEGY_GUIDE.md) - Planning strategy
- [IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md](./IMPACT_ANALYSIS_TRACEABILITY_GUIDE.md) - Impact analysis

---

**Version**: 1.0.0  
**Created**: November 10, 2025  
**Format**: Mermaid.js Diagrams

**Note**: These diagrams can be rendered in:

- GitHub/GitLab markdown viewers
- VS Code with Mermaid extension
- Mermaid Live Editor (https://mermaid.live)
- Documentation sites (Docusaurus, VitePress, etc.)

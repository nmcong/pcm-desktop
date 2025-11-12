# BPMN Workflow System

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Components](#components)
- [BPMN Generation](#bpmn-generation)
- [Workflow Analysis](#workflow-analysis)
- [User Interface](#user-interface)
- [Technical Implementation](#technical-implementation)
- [Usage Examples](#usage-examples)
- [Extending the System](#extending-the-system)

---

## Overview

The BPMN Workflow System automatically generates and visualizes BPMN 2.0 compliant workflow diagrams from screen navigation and event data. It analyzes screen relationships to build comprehensive workflows and displays them using the industry-standard bpmn-js library.

### Key Features

- **🔄 Auto Workflow Generation**: Analyzes screens and events to build workflows
- **📊 BPMN 2.0 Standard**: Industry-standard format
- **🎨 Interactive Viewer**: Pan, zoom, and navigate diagrams
- **💾 Export**: Download as `.bpmn` files
- **🚀 Zero Configuration**: Works out of the box
- **📱 Responsive**: Adapts to different screen sizes

### Technology Stack

- **bpmn-js**: BPMN viewer and renderer
- **BPMN 2.0**: Standard workflow definition format
- **Custom Algorithm**: Workflow generation from screen events

---

## Architecture

### System Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    User Action                               │
│         Click "Generate" in Workflow Tab                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              ScreenWorkflowManager.js                        │
│  - Load all screens in project from IndexedDB                │
│  - Analyze screen events (navigate, branch)                  │
│  - Build workflow paths using BFS algorithm                  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   BpmnEngine.js                              │
│  - Convert workflow data to BPMN 2.0 XML                     │
│  - Create Start Event, Tasks, Sequence Flows, End Event      │
│  - Apply auto-layout algorithm                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   BpmnViewer.js                              │
│  - Load bpmn-js library from vendor folder                   │
│  - Initialize viewer with canvas container                   │
│  - Import and render BPMN XML                                │
│  - Provide zoom, pan, export functionality                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Interactive Diagram                         │
│  - Pan and zoom with mouse                                   │
│  - Professional BPMN rendering                               │
│  - Export to .bpmn file                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## Components

### 1. ScreenWorkflowManager

**File**: `apps/pcm-webapp/public/js/components/ScreenWorkflowManager.js`

**Responsibilities:**

- Manage workflow tab UI
- Generate workflows from screen data
- Display workflow list and viewer
- Handle user interactions

**Key Methods:**

```javascript
class ScreenWorkflowManager {
  constructor(screen, onUpdate)

  // UI Creation
  createWorkflowTab()          // Main tab structure
  renderWorkflowsList()        // List of workflows
  selectWorkflow(workflow)     // Select and display workflow
  displayWorkflow(workflow)    // Render BPMN diagram

  // Workflow Generation
  generateWorkflows()          // Generate all workflows
  analyzeScreenFlows()         // Analyze screen relationships

  // BPMN Integration
  createBpmnToolbar()          // Toolbar with actions
  createSimpleFallbackView()   // Fallback if BPMN fails
}
```

---

### 2. BpmnEngine

**File**: `apps/pcm-webapp/public/js/components/BpmnEngine.js`

**Responsibilities:**

- Convert workflow data to BPMN XML
- Generate BPMN elements (events, tasks, flows)
- Format XML according to BPMN 2.0 spec
- Export to `.bpmn` files

**Key Methods:**

```javascript
class BpmnEngine {
  // Core Conversion
  convertWorkflowToBpmn(workflow)    // Main conversion method
  createEmptyBpmn()                  // Empty diagram template

  // BPMN Elements
  createStartEvent(id, x, y)         // Start event (circle)
  createTask(id, name, x, y)         // Task (rectangle)
  createEndEvent(id, x, y)           // End event (circle)
  createSequenceFlow(id, src, tgt)   // Arrow between elements

  // Diagram Layout
  createShape(id, x, y, w, h)        // Visual shape
  createEdge(id, src, tgt)           // Visual connector

  // Utilities
  buildBpmnXml(...)                  // Assemble complete XML
  escapeXml(text)                    // Escape special characters
  exportToBpmnFile(workflow)         // Download as file
}
```

---

### 3. BpmnViewer

**File**: `apps/pcm-webapp/public/js/components/BpmnViewer.js`

**Responsibilities:**

- Load bpmn-js library dynamically
- Initialize BPMN viewer
- Render BPMN XML
- Provide viewer controls

**Key Methods:**

```javascript
class BpmnViewer {
  // Initialization
  async initialize(containerElement)  // Setup viewer
  async loadBpmnJS()                 // Load bpmn-js library

  // Diagram Operations
  async loadDiagram(xml)             // Load and display BPMN
  async exportSVG()                  // Export as SVG
  zoomToFit()                        // Fit diagram to viewport
  destroy()                          // Cleanup
}
```

---

## BPMN Generation

### Workflow Structure

Each workflow contains:

```javascript
{
  id: "wf_123456789",
  name: "Workflow: Login to Dashboard",
  description: "Auto-generated workflow from Login Screen to Dashboard",
  startScreenId: 1,
  steps: [
    {
      screenId: 1,
      screenName: "Login Screen",
      type: "screen",
      depth: 0
    },
    {
      screenId: 2,
      screenName: "Dashboard",
      type: "screen",
      depth: 1
    }
  ],
  createdAt: 1699285200000
}
```

### BPMN XML Structure

Generated BPMN follows this structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI">

  <!-- Process Definition -->
  <bpmn:process id="Process_123" name="Workflow Name">

    <!-- Start Event -->
    <bpmn:startEvent id="StartEvent_123" name="Start">
      <bpmn:outgoing>Flow_123</bpmn:outgoing>
    </bpmn:startEvent>

    <!-- Task for Each Screen -->
    <bpmn:task id="Task_124" name="Login Screen">
      <bpmn:incoming>Flow_123</bpmn:incoming>
      <bpmn:outgoing>Flow_124</bpmn:outgoing>
    </bpmn:task>

    <bpmn:task id="Task_125" name="Dashboard">
      <bpmn:incoming>Flow_124</bpmn:incoming>
      <bpmn:outgoing>Flow_125</bpmn:outgoing>
    </bpmn:task>

    <!-- End Event -->
    <bpmn:endEvent id="EndEvent_126" name="End">
      <bpmn:incoming>Flow_125</bpmn:incoming>
    </bpmn:endEvent>

    <!-- Sequence Flows (arrows) -->
    <bpmn:sequenceFlow id="Flow_123" sourceRef="StartEvent_123" targetRef="Task_124" />
    <bpmn:sequenceFlow id="Flow_124" sourceRef="Task_124" targetRef="Task_125" />
    <bpmn:sequenceFlow id="Flow_125" sourceRef="Task_125" targetRef="EndEvent_126" />
  </bpmn:process>

  <!-- Diagram Information (visual layout) -->
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_123">

      <!-- Visual positions and sizes -->
      <bpmndi:BPMNShape id="StartEvent_123_di" bpmnElement="StartEvent_123">
        <dc:Bounds x="150" y="150" width="36" height="36" />
      </bpmndi:BPMNShape>

      <bpmndi:BPMNShape id="Task_124_di" bpmnElement="Task_124">
        <dc:Bounds x="250" y="120" width="100" height="80" />
      </bpmndi:BPMNShape>

      <!-- Edges (visual connectors) -->
      <bpmndi:BPMNEdge id="Flow_123_di" bpmnElement="Flow_123">
        <di:waypoint x="200" y="200" />
        <di:waypoint x="300" y="200" />
      </bpmndi:BPMNEdge>

    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
```

---

## Workflow Analysis

### Analysis Algorithm

The system uses Breadth-First Search (BFS) to traverse screen relationships:

```javascript
analyzeScreenFlows(screens, projectId) {
  const screenMap = new Map(screens.map(s => [s.id, s]));
  const workflows = [];
  const MAX_STEPS = 20;
  const MAX_DEPTH = 10;

  screens.forEach(startScreen => {
    const queue = [{ screen: startScreen, path: [], depth: 0 }];
    const visitedPaths = new Set();

    while (queue.length > 0) {
      const { screen, path, depth } = queue.shift();

      // Check limits
      if (depth >= MAX_DEPTH || path.length >= MAX_STEPS) continue;

      // Avoid cycles
      const pathKey = path.map(s => s.screenId).join("->") + "->" + screen.id;
      if (visitedPaths.has(pathKey)) continue;
      visitedPaths.add(pathKey);

      // Build current path
      const newPath = [
        ...path,
        {
          screenId: screen.id,
          screenName: screen.name,
          type: "screen",
          depth: depth
        }
      ];

      // If path is long enough and ends here, create workflow
      if (newPath.length > 1 && !screen.events?.length) {
        workflows.push({
          id: generateId(),
          name: `Workflow: ${newPath[0].screenName} to ${newPath[newPath.length - 1].screenName}`,
          description: `Auto-generated workflow`,
          startScreenId: newPath[0].screenId,
          steps: newPath,
          createdAt: Date.now()
        });
      }

      // Process screen events
      if (screen.events && screen.events.length > 0) {
        screen.events.forEach(event => {
          if (event.actionType === "navigate" && event.targetScreen) {
            const targetScreen = screenMap.get(parseInt(event.targetScreen));
            if (targetScreen) {
              queue.push({
                screen: targetScreen,
                path: newPath,
                depth: depth + 1
              });
            }
          } else if (event.actionType === "branch" && event.branchOptions) {
            event.branchOptions.forEach(option => {
              if (option.actionType === "navigate" && option.targetScreen) {
                const targetScreen = screenMap.get(parseInt(option.targetScreen));
                if (targetScreen) {
                  queue.push({
                    screen: targetScreen,
                    path: newPath,
                    depth: depth + 1
                  });
                }
              }
            });
          }
        });
      }
    }
  });

  // Remove duplicate workflows
  const uniqueWorkflows = [];
  const workflowPaths = new Set();
  workflows.forEach(wf => {
    const pathKey = wf.steps.map(step => step.screenId).join("->");
    if (!workflowPaths.has(pathKey)) {
      workflowPaths.add(pathKey);
      uniqueWorkflows.push(wf);
    }
  });

  return uniqueWorkflows;
}
```

### Event Types

The system recognizes two event types:

1. **Navigate Events**
   - Direct navigation to another screen
   - Creates a single path in the workflow

2. **Branch Events**
   - Conditional navigation based on user input
   - Creates multiple possible paths
   - Each branch option can lead to different screens

---

## User Interface

### Layout

```
┌─────────────────────────────────────────────────────────────┐
│                      Workflow Tab                            │
├───────────┬─────────────────────────────────────────────────┤
│           │                                                  │
│  300px    │              BPMN Viewer                         │
│  Fixed    │          (Remaining Space)                       │
│           │                                                  │
│ ┌───────┐ │  ┌──────────────────────────────────────────┐  │
│ │Generate│ │  │ Toolbar: Workflow Name | [Fit] [Export] │  │
│ └───────┘ │  ├──────────────────────────────────────────┤  │
│           │  │                                           │  │
│ [Search]  │  │    ○ Start                                │  │
│           │  │     ↓                                     │  │
│ ┌───────┐ │  │  ┌──────────┐                            │  │
│ │  WF1  │ │  │  │ Screen 1 │                            │  │
│ └───────┘ │  │  └──────────┘                            │  │
│ ┌───────┐ │  │     ↓                                     │  │
│ │  WF2  │ │  │  ┌──────────┐                            │  │
│ └───────┘ │  │  │ Screen 2 │                            │  │
│           │  │  └──────────┘                            │  │
│ 3 steps   │  │     ↓                                     │  │
│           │  │    ○ End                                  │  │
│           │  │                                           │  │
│           │  └──────────────────────────────────────────┘  │
└───────────┴─────────────────────────────────────────────────┘
```

### Toolbar Actions

1. **Zoom to Fit** (🔍)
   - Automatically adjusts zoom level to fit entire diagram
   - Keyboard shortcut: N/A

2. **Export** (💾)
   - Downloads workflow as `.bpmn` file
   - Can be imported into other BPMN tools (Camunda, Signavio, etc.)

### Workflow List

Each workflow item displays:

- Workflow name
- Number of steps
- Creation date

Clicking a workflow:

- Highlights the item
- Displays BPMN diagram in viewer

---

## Technical Implementation

### Integration with ProjectDetailPage

```javascript
// In ProjectDetailPage.js

createProjectWorkflowContent() {
  const container = document.createElement("div");
  container.className = "project-workflow-content";

  // Create pseudo-screen object for workflow manager
  const projectWorkflowData = {
    id: this.projectId,
    projectId: this.projectId,
    name: this.project.name,
    workflows: this.project.workflows || []
  };

  // Use ScreenWorkflowManager
  const workflowManager = new ScreenWorkflowManager(
    projectWorkflowData,
    async () => {
      // Save workflows back to project
      await databaseManager.updateProject(this.projectId, {
        workflows: projectWorkflowData.workflows
      });
      this.project.workflows = projectWorkflowData.workflows;
    }
  );

  const workflowTab = workflowManager.createWorkflowTab();
  container.appendChild(workflowTab);

  return container;
}
```

### BPMN Library Loading

```javascript
// In BpmnViewer.js

async loadBpmnJS() {
  return new Promise((resolve, reject) => {
    // Load CSS
    const cssLink = document.createElement("link");
    cssLink.rel = "stylesheet";
    cssLink.href = "./public/vendor/bpmn-js/bpmn-js.css";
    document.head.appendChild(cssLink);

    const diagramCssLink = document.createElement("link");
    diagramCssLink.rel = "stylesheet";
    diagramCssLink.href = "./public/vendor/bpmn-js/diagram-js.css";
    document.head.appendChild(diagramCssLink);

    // Load JS
    const script = document.createElement("script");
    script.src = "./public/vendor/bpmn-js/bpmn-js.js";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load bpmn-js"));
    document.head.appendChild(script);
  });
}
```

### Workflow Storage

Workflows are stored in the project object in IndexedDB:

```javascript
{
  id: 1,
  name: "Project Name",
  // ... other project fields
  workflows: [
    {
      id: "wf_123",
      name: "Workflow Name",
      description: "...",
      startScreenId: 1,
      steps: [...],
      createdAt: 1699285200000
    }
  ]
}
```

---

## Usage Examples

### Example 1: Generate Workflows

1. Open a project
2. Click the "Workflow" tab (at project level)
3. Click "Generate" button
4. System analyzes all screens and events
5. Creates workflows automatically
6. Displays list of generated workflows

**Result:**

```
✓ 5 workflows generated successfully!

1. Workflow: Login to Dashboard (3 steps)
2. Workflow: Dashboard to Settings (2 steps)
3. Workflow: Login to Profile (4 steps)
4. Workflow: Dashboard to Logout (2 steps)
5. Workflow: Settings to Change Password (3 steps)
```

---

### Example 2: View BPMN Diagram

1. Click on a workflow in the list
2. BPMN diagram renders in the viewer
3. Use mouse to pan and zoom
4. See professional BPMN notation:
   - Circles for Start/End events
   - Rectangles for Tasks (screens)
   - Arrows for Sequence Flows

---

### Example 3: Export Workflow

1. Select a workflow
2. Click "Export" button in toolbar
3. Downloads `Workflow_Name.bpmn` file
4. Can be imported into:
   - Camunda Modeler
   - Signavio Process Manager
   - Bizagi Modeler
   - Any BPMN 2.0 compliant tool

---

### Example 4: Complex Branching

For a workflow with branches:

```
Login Screen
  ↓
Check User Type (Branch)
  ├─→ Admin → Admin Dashboard
  └─→ User → User Dashboard
```

The system generates:

1. **Workflow 1**: Login → Admin Dashboard (2 steps)
2. **Workflow 2**: Login → User Dashboard (2 steps)

Each branch becomes a separate workflow.

---

## Extending the System

### Adding Gateway Support

To add BPMN gateways for branches:

```javascript
// In BpmnEngine.js

createExclusiveGateway(id, name, x, y) {
  return `    <bpmn:exclusiveGateway id="${id}" name="${this.escapeXml(name)}">
      <bpmn:incoming>Flow_to_${id}</bpmn:incoming>
      <bpmn:outgoing>Flow_${id}_option1</bpmn:outgoing>
      <bpmn:outgoing>Flow_${id}_option2</bpmn:outgoing>
    </bpmn:exclusiveGateway>
`;
}

// In convertWorkflowToBpmn, detect branch events:
if (event.actionType === "branch") {
  const gatewayId = this.generateId("Gateway");
  bpmnElements += this.createExclusiveGateway(
    gatewayId,
    event.branchQuestion,
    x,
    y
  );

  // Add flows for each branch option
  event.branchOptions.forEach(option => {
    const flowId = this.generateId("Flow");
    bpmnFlows += this.createSequenceFlow(
      flowId,
      gatewayId,
      targetTaskId,
      option.answer
    );
  });
}
```

---

### Custom Layout Algorithm

To improve diagram layout:

```javascript
// Implement a better auto-layout
class BpmnLayoutEngine {
  calculatePositions(workflow) {
    const positions = new Map();
    const HORIZONTAL_SPACING = 150;
    const VERTICAL_SPACING = 120;

    let currentX = 150;
    let currentY = 150;

    workflow.steps.forEach((step, index) => {
      positions.set(step.screenId, {
        x: currentX,
        y: currentY,
      });

      // Move to next position
      if (this.hasBranch(step)) {
        // Branch layout logic
        currentY += VERTICAL_SPACING * 2;
      } else {
        currentX += HORIZONTAL_SPACING;
      }
    });

    return positions;
  }
}
```

---

### Adding Parallel Gateways

For concurrent tasks:

```javascript
createParallelGateway(id, name) {
  return `    <bpmn:parallelGateway id="${id}" name="${this.escapeXml(name)}">
      <bpmn:incoming>Flow_to_${id}</bpmn:incoming>
      <bpmn:outgoing>Flow_${id}_fork1</bpmn:outgoing>
      <bpmn:outgoing>Flow_${id}_fork2</bpmn:outgoing>
    </bpmn:parallelGateway>
`;
}
```

---

### Workflow Validation

Add validation before generation:

```javascript
validateWorkflow(workflow) {
  const errors = [];

  // Check for cycles
  if (this.hasCycles(workflow)) {
    errors.push("Workflow contains cycles");
  }

  // Check for orphaned screens
  if (this.hasOrphanedScreens(workflow)) {
    errors.push("Workflow has unreachable screens");
  }

  // Check depth limits
  if (workflow.steps.length > MAX_STEPS) {
    errors.push(`Workflow exceeds maximum steps (${MAX_STEPS})`);
  }

  return {
    valid: errors.length === 0,
    errors: errors
  };
}
```

---

## Best Practices

### 1. Workflow Design

✅ **DO:**

- Keep workflows focused and clear
- Use descriptive screen names
- Limit workflow depth to avoid complexity
- Group related screens together
- Document branch conditions

❌ **DON'T:**

- Create workflows with too many steps (>20)
- Have circular references
- Skip important intermediate screens
- Use generic names like "Screen1", "Screen2"

---

### 2. BPMN Generation

✅ **DO:**

- Follow BPMN 2.0 specification
- Use standard element types
- Include proper namespaces
- Validate generated XML
- Handle special characters in names

❌ **DON'T:**

- Generate invalid XML
- Skip diagram layout information
- Forget to escape XML entities
- Create malformed BPMN structures

---

### 3. Performance

✅ **DO:**

- Limit BFS depth to prevent infinite loops
- Cache generated workflows
- Use efficient data structures (Map, Set)
- Debounce generation triggers
- Show progress indicators

❌ **DON'T:**

- Generate workflows on every render
- Process entire database for each workflow
- Block UI during generation
- Ignore memory limits

---

### 4. User Experience

✅ **DO:**

- Show generation progress
- Provide clear error messages
- Allow workflow regeneration
- Enable zoom and pan
- Support keyboard shortcuts

❌ **DON'T:**

- Freeze UI during generation
- Show cryptic errors
- Force users to regenerate constantly
- Make diagrams non-interactive

---

## Troubleshooting

### Issue: BPMN Library Fails to Load

**Error:**

```
Failed to load bpmn-js
GET http://localhost:63342/vendor/bpmn-js/bpmn-js.js 404
```

**Solution:**
Check file paths in `BpmnViewer.js`:

```javascript
cssLink.href = "./public/vendor/bpmn-js/bpmn-js.css";
script.src = "./public/vendor/bpmn-js/bpmn-js.js";
```

---

### Issue: No Workflows Generated

**Symptoms:**

- "No workflows found" message
- Empty workflow list

**Possible Causes:**

1. No screens in project
2. Screens have no events
3. No navigation events defined

**Solution:**

1. Ensure screens exist: Check IndexedDB
2. Add events to screens
3. Define navigation or branch events

---

### Issue: Invalid BPMN XML

**Symptoms:**

- Diagram fails to render
- Console errors about XML parsing

**Solution:**

1. Validate XML structure
2. Check for unescaped special characters
3. Ensure all IDs are unique
4. Verify namespace declarations

---

### Issue: Circular Workflows

**Symptoms:**

- Generation takes too long
- Browser freezes

**Solution:**

1. Check `MAX_DEPTH` and `MAX_STEPS` limits
2. Use `visitedPaths` Set to detect cycles
3. Implement cycle detection:

```javascript
const visitedPaths = new Set();
const pathKey = path.map(s => s.screenId).join("->");
if (visitedPaths.has(pathKey)) continue;
visitedPaths.add(pathKey);
```

---

## Future Enhancements

1. **Advanced BPMN Elements**
   - Exclusive Gateways for branches
   - Parallel Gateways for concurrent flows
   - Intermediate Events
   - Subprocesses

2. **Interactive Editing**
   - Drag and drop to rearrange
   - Add/remove screens
   - Edit event conditions
   - Save changes back to database

3. **Export Formats**
   - SVG for presentations
   - PNG for documentation
   - PDF for reports
   - JSON for data exchange

4. **Workflow Analytics**
   - Most used paths
   - Bottleneck detection
   - Performance metrics
   - User journey analysis

5. **Collaboration**
   - Share workflows with team
   - Comments and annotations
   - Version history
   - Approval workflows

---

## References

- **BPMN 2.0 Specification**: https://www.omg.org/spec/BPMN/2.0/
- **bpmn-js Library**: https://bpmn.io/toolkit/bpmn-js/
- **Implementation**: `apps/pcm-webapp/public/js/components/ScreenWorkflowManager.js`
- **BPMN Engine**: `apps/pcm-webapp/public/js/components/BpmnEngine.js`
- **BPMN Viewer**: `apps/pcm-webapp/public/js/components/BpmnViewer.js`

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Author**: PCM Development Team

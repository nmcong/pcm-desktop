/**
 * AIPanel Planning Integration Example
 *
 * This file shows how to integrate planning capabilities into AIPanel.
 *
 * Copy relevant parts to your AIPanel.js implementation.
 */
import { buildSystemPrompt } from "../services/EnhancedPromptService.js";
import { detectIntent } from "../services/IntentDetectionService.js";
import {
  needsPlanning,
  generatePlan,
  formatPlan,
  validateStepResult,
} from "../services/PlanningService.js";

/**
 * STEP 1: Update handleSendMessage to include planning
 */
async function handleSendMessage_WithPlanning(e) {
  e.preventDefault();

  const message = this.messageInput.value.trim();
  if (!message) return;

  // Clear input and add user message to chat
  this.messageInput.value = "";
  this.chatView.addMessage("user", message);

  try {
    // === PLANNING INTEGRATION START ===

    // 1. Detect intent
    console.log("[AIPanel] Detecting intent...");
    const intent = detectIntent(message);
    console.log("[AIPanel] Intent detected:", {
      category: intent.category,
      confidence: intent.confidence,
      entities: intent.entities,
    });

    // 2. Check if planning is needed
    const shouldPlan = needsPlanning(message, intent);
    console.log("[AIPanel] Needs planning:", shouldPlan);

    // 3. Generate plan if needed
    let plan = null;
    if (shouldPlan) {
      plan = generatePlan(message, intent);
      console.log("[AIPanel] Execution plan generated:", plan);

      // 4. Show plan to user (optional but recommended)
      const planMessage = `📋 **Execution Plan** (${plan.complexity})\n\n${formatPlan(plan)}`;
      this.chatView.addMessage("system", planMessage, {
        type: "plan",
        collapsible: true,
        metadata: { plan },
      });

      // Optional: Show progress indicator
      this.showProcessingIndicator(
        `Processing ${plan.steps.length} steps (~${plan.estimatedTime}s)...`,
      );
    }

    // === PLANNING INTEGRATION END ===

    // 5. Get AI response with planning context
    await this.getAIResponseWithPlanning(message, intent, plan);
  } catch (error) {
    console.error("[AIPanel] Error:", error);
    this.chatView.addMessage("error", `Error: ${error.message}`);
  }
}

/**
 * STEP 2: Create new method to handle AI response with planning
 */
async function getAIResponseWithPlanning(message, intent, plan) {
  try {
    // Build enhanced system prompt with planning context
    const systemPrompt = await buildSystemPrompt({
      includeStatistics: true,
      includeExamples: true,
      includeGuidelines: true,
      includePlanning: plan !== null, // Enable planning mode
      plan: plan, // Pass the plan
      userContext: this.getCurrentUserContext(), // Your existing method
    });

    // Prepare messages for AI
    const messages = [
      { role: "system", content: systemPrompt },
      ...this.conversationHistory, // Your existing conversation history
      { role: "user", content: message },
    ];

    // Call AI service
    const response = await this.aiService.chat({
      messages,
      tools: this.availableTools, // Your existing tools
      temperature: 0.3, // Lower temperature for more consistent planning
    });

    // Process response
    await this.processAIResponse(response, plan);
  } catch (error) {
    console.error("[AIPanel] AI response error:", error);
    throw error;
  }
}

/**
 * STEP 3: Update processAIResponse to handle planning validation
 */
async function processAIResponse(response, plan) {
  // Handle tool calls
  if (response.tool_calls && response.tool_calls.length > 0) {
    console.log(
      `[AIPanel] Processing ${response.tool_calls.length} tool calls`,
    );

    const results = [];

    // If we have a plan, validate against it
    if (plan) {
      // Track which steps have been executed
      const executedSteps = new Set();

      for (const toolCall of response.tool_calls) {
        // Find corresponding step in plan
        const step = plan.steps.find((s) => s.tool === toolCall.function.name);

        if (step) {
          console.log(`[AIPanel] Executing planned step ${step.stepNumber}...`);

          // Check dependencies
          if (step.dependsOn && step.dependsOn.length > 0) {
            const depsReady = step.dependsOn.every((depStep) =>
              executedSteps.has(depStep),
            );

            if (!depsReady) {
              console.warn(
                `[AIPanel] Skipping step ${step.stepNumber} - dependencies not ready`,
              );
              results.push({
                tool: toolCall.function.name,
                success: false,
                error: "Dependencies not met",
              });
              continue;
            }
          }

          // Execute tool
          const result = await this.executeTool(
            toolCall.function.name,
            toolCall.function.arguments,
          );

          // Validate result
          const validation = validateStepResult(step, result);

          if (!validation.success) {
            console.warn(
              `[AIPanel] Step ${step.stepNumber} validation failed:`,
              validation.message,
            );
          } else {
            console.log(
              `[AIPanel] Step ${step.stepNumber} completed:`,
              validation.message,
            );
            executedSteps.add(step.stepNumber);
          }

          results.push({
            ...result,
            stepNumber: step.stepNumber,
            validation,
          });
        } else {
          // Tool call not in plan (AI deviated)
          console.warn(
            `[AIPanel] Tool '${toolCall.function.name}' not in plan, executing anyway...`,
          );
          const result = await this.executeTool(
            toolCall.function.name,
            toolCall.function.arguments,
          );
          results.push(result);
        }
      }

      // Check if all critical steps were executed
      const criticalStepsCompleted = plan.steps
        .filter((s) => !s.optional)
        .every((s) => executedSteps.has(s.stepNumber));

      if (!criticalStepsCompleted) {
        console.warn(
          "[AIPanel] Not all critical steps completed:",
          plan.steps.filter(
            (s) => !s.optional && !executedSteps.has(s.stepNumber),
          ),
        );
      }
    } else {
      // No plan - execute normally
      for (const toolCall of response.tool_calls) {
        const result = await this.executeTool(
          toolCall.function.name,
          toolCall.function.arguments,
        );
        results.push(result);
      }
    }

    // Continue conversation with results
    await this.continueConversationWithResults(results);
  } else if (response.content) {
    // Final response
    this.chatView.addMessage("assistant", response.content);
    this.hideProcessingIndicator();
  }
}

/**
 * STEP 4: Add helper methods
 */

function showProcessingIndicator(message) {
  // Your implementation - show loading/progress UI
  const indicator = document.createElement("div");
  indicator.className = "processing-indicator";
  indicator.id = "ai-processing-indicator";
  indicator.innerHTML = `
    <div class="spinner"></div>
    <span>${message}</span>
  `;
  this.chatView.container.appendChild(indicator);
}

function hideProcessingIndicator() {
  const indicator = document.getElementById("ai-processing-indicator");
  if (indicator) {
    indicator.remove();
  }
}

function getCurrentUserContext() {
  // Your implementation - get current project/screen context
  return {
    currentProject: this.currentProject || null,
    currentScreen: this.currentScreen || null,
    recentActivity: this.recentActivity || [],
  };
}

/**
 * STEP 5: Update your AIPanel class to use planning
 */

// Example complete integration:
class AIPanel_WithPlanning {
  constructor() {
    // ... existing constructor code ...

    // Add planning flag
    this.planningEnabled = true; // Can be toggled by user
  }

  async handleSendMessage(e) {
    // Use the planning-enabled version
    return handleSendMessage_WithPlanning.call(this, e);
  }

  async getAIResponseWithPlanning(message, intent, plan) {
    return getAIResponseWithPlanning.call(this, message, intent, plan);
  }

  async processAIResponse(response, plan) {
    return processAIResponse.call(this, response, plan);
  }

  // Helper methods
  showProcessingIndicator(message) {
    return showProcessingIndicator.call(this, message);
  }

  hideProcessingIndicator() {
    return hideProcessingIndicator.call(this);
  }

  getCurrentUserContext() {
    return getCurrentUserContext.call(this);
  }
}

/**
 * STEP 6: Add UI toggle for planning (optional)
 */

function addPlanningToggle() {
  const toggle = document.createElement("label");
  toggle.className = "planning-toggle";
  toggle.innerHTML = `
    <input type="checkbox" id="planning-toggle" checked>
    <span>Enable AI Planning</span>
    <div class="tooltip">
      When enabled, AI will create execution plans for complex queries.
      This improves accuracy but may take slightly longer.
    </div>
  `;

  toggle.querySelector("input").addEventListener("change", (e) => {
    this.planningEnabled = e.target.checked;
    console.log("[AIPanel] Planning enabled:", this.planningEnabled);
  });

  // Add to your AI panel header
  this.header.appendChild(toggle);
}

/**
 * STEP 7: Add CSS for planning UI elements
 */

const PLANNING_CSS = `
/* Planning indicator */
.processing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f0f7ff;
  border-left: 3px solid #0066cc;
  border-radius: 4px;
  margin: 8px 0;
  font-size: 14px;
  color: #333;
}

.processing-indicator .spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #0066cc;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Plan message styling */
.chat-message.plan {
  background: #f8f9fa;
  border-left: 4px solid #28a745;
}

.chat-message.plan.collapsible .plan-content {
  max-height: 200px;
  overflow-y: auto;
}

.chat-message.plan .plan-header {
  font-weight: 600;
  margin-bottom: 8px;
  cursor: pointer;
}

.chat-message.plan .plan-header::before {
  content: "▼ ";
  display: inline-block;
  transition: transform 0.2s;
}

.chat-message.plan.collapsed .plan-header::before {
  transform: rotate(-90deg);
}

.chat-message.plan.collapsed .plan-content {
  display: none;
}

/* Planning toggle */
.planning-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  cursor: pointer;
  position: relative;
}

.planning-toggle input[type="checkbox"] {
  cursor: pointer;
}

.planning-toggle .tooltip {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  background: #333;
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  width: 250px;
  margin-top: 4px;
  z-index: 1000;
}

.planning-toggle:hover .tooltip {
  display: block;
}

/* Step indicators in responses */
.step-indicator {
  display: inline-block;
  background: #0066cc;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-right: 6px;
}

.step-indicator.completed {
  background: #28a745;
}

.step-indicator.failed {
  background: #dc3545;
}

.step-indicator.skipped {
  background: #6c757d;
}
`;

/**
 * USAGE SUMMARY
 *
 * 1. Import planning services at top of AIPanel.js:
 *    import { detectIntent } from "./services/IntentDetectionService.js";
 *    import { needsPlanning, generatePlan, formatPlan, validateStepResult } from "./services/PlanningService.js";
 *
 * 2. Update buildSystemPrompt call to include planning:
 *    const systemPrompt = await buildSystemPrompt({
 *      includePlanning: true,
 *      plan: plan,
 *      ...otherOptions
 *    });
 *
 * 3. Update handleSendMessage to use planning flow
 *
 * 4. Add validation in tool execution loop
 *
 * 5. Add UI elements (indicator, toggle) and CSS
 *
 * 6. Test with both simple and complex queries
 */

export default AIPanel_WithPlanning;

/**
 * TESTING EXAMPLES
 */

// Simple query (should not trigger planning):
// "Show me project #5"
// Expected: Direct execution, no plan shown

// Complex query (should trigger planning):
// "Analyze the impact of adding a phone field to the register screen"
// Expected: Plan shown with 5-6 steps, systematic execution

// Analysis query (should trigger planning):
// "How does the authentication system work?"
// Expected: Plan with search → fetch → analyze → synthesize

// Comparison query (should trigger planning):
// "Compare the authentication and authorization projects"
// Expected: Plan with identify → fetch both → compare → report

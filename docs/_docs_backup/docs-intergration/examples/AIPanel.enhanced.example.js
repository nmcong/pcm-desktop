/**
 * AIPanel - Enhanced Version with Advanced AI Capabilities
 *
 * This is an EXAMPLE file showing how to integrate:
 * - Intent Detection
 * - Enhanced Prompts
 * - Advanced Query Functions
 *
 * To use:
 * 1. Copy relevant methods to your AIPanel.js
 * 2. Or rename this file to AIPanel.js (backup old one first)
 */
import {
    analyzeQueryComplexity,
    buildConversationContext,
    buildSystemPrompt,
    enrichUserMessage,
} from "../services/EnhancedPromptService.js";
import functionCallingService from "../services/FunctionCallingService.js";
import {detectIntent, formatIntent,} from "../services/IntentDetectionService.js";
import providerRegistry from "../services/ProviderRegistry.js";
import ChatView from "./ChatView.js";

// Assume you have this

export class AIPanel {
    constructor() {
        this.element = null;
        this.messageInput = null;
        this.chatView = null;
        this.conversationHistory = [];
        this.isProcessing = false;

        // Context tracking
        this.currentProject = null;
        this.currentScreen = null;
        this.recentActivity = [];

        // Settings
        this.settings = {
            enableIntentDetection: true,
            enableEnhancedPrompts: true,
            includeSystemStats: true,
            enableDebugLogs: true,
        };
    }

    /**
     * Initialize the panel
     */
    async init() {
        this.element = document.getElementById("ai-panel");
        this.messageInput = this.element.querySelector("#ai-message-input");
        this.chatView = new ChatView();

        // Event listeners
        this.messageInput.addEventListener("submit", (e) =>
            this.handleSendMessage(e),
        );

        console.log("[AIPanel] Enhanced version initialized");
    }

    /**
     * Handle send message - ENHANCED VERSION
     */
    async handleSendMessage(e) {
        e.preventDefault();

        const message = this.messageInput.value.trim();
        if (!message || this.isProcessing) return;

        this.isProcessing = true;

        try {
            // === STEP 1: INTENT DETECTION ===
            let intent = null;
            if (this.settings.enableIntentDetection) {
                intent = detectIntent(message);

                if (this.settings.enableDebugLogs) {
                    console.log("[AIPanel] Detected intent:", formatIntent(intent));
                }

                // Show intent in UI (optional)
                this.chatView.showIntent(intent);
            }

            // === STEP 2: ENRICH USER MESSAGE ===
            let enrichedMessage = message;
            if (this.settings.enableEnhancedPrompts) {
                const userContext = {
                    currentProject: this.currentProject,
                    currentScreen: this.currentScreen,
                    recentActivity: this.recentActivity.slice(-5), // Last 5 activities
                };

                enrichedMessage = await enrichUserMessage(message, userContext);

                if (this.settings.enableDebugLogs) {
                    console.log("[AIPanel] Enriched message:", enrichedMessage);
                }
            }

            // === STEP 3: ANALYZE QUERY COMPLEXITY ===
            let complexity = null;
            if (this.settings.enableEnhancedPrompts) {
                complexity = analyzeQueryComplexity(message);

                if (this.settings.enableDebugLogs) {
                    console.log("[AIPanel] Query complexity:", complexity);
                }

                // Show estimated steps in UI
                if (complexity.estimatedSteps > 1) {
                    this.chatView.showProcessingSteps(complexity.estimatedSteps);
                }
            }

            // === STEP 4: ADD TO CONVERSATION HISTORY ===
            this.conversationHistory.push({
                role: "user",
                content: enrichedMessage,
                metadata: {
                    originalMessage: message,
                    intent: intent,
                    complexity: complexity,
                    timestamp: new Date().toISOString(),
                },
            });

            // Display user message
            this.chatView.addMessage("user", message);

            // === STEP 5: GET AI RESPONSE ===
            await this.getEnhancedAIResponse(intent, complexity);
        } catch (error) {
            console.error("[AIPanel] Error handling message:", error);
            this.chatView.addMessage("assistant", `Error: ${error.message}`);
        } finally {
            this.isProcessing = false;
            this.messageInput.value = "";
        }
    }

    /**
     * Get AI response with enhanced prompts and context
     */
    async getEnhancedAIResponse(intent, complexity) {
        try {
            this.chatView.showTypingIndicator();

            // === BUILD ENHANCED SYSTEM PROMPT ===
            let systemPrompt = "";
            if (this.settings.enableEnhancedPrompts) {
                const userContext = {
                    currentProject: this.currentProject,
                    currentScreen: this.currentScreen,
                    recentActivity: this.recentActivity.slice(-5),
                };

                systemPrompt = await buildSystemPrompt({
                    includeStatistics: this.settings.includeSystemStats,
                    includeExamples: true,
                    includeGuidelines: true,
                    userContext: userContext,
                });

                // Add conversation context
                if (this.conversationHistory.length > 1) {
                    const conversationContext = buildConversationContext(
                        this.conversationHistory,
                        5, // Last 5 messages
                    );
                    systemPrompt += conversationContext;
                }

                if (this.settings.enableDebugLogs) {
                    console.log("[AIPanel] System prompt length:", systemPrompt.length);
                }
            }

            // === PREPARE MESSAGES ===
            const messages = [];

            if (systemPrompt) {
                messages.push({
                    role: "system",
                    content: systemPrompt,
                });
            }

            // Add conversation history (last N messages)
            const historyLimit = 10;
            const recentHistory = this.conversationHistory.slice(-historyLimit);
            messages.push(
                ...recentHistory.map((msg) => ({
                    role: msg.role,
                    content: msg.content,
                })),
            );

            // === GET ACTIVE PROVIDER ===
            const provider = providerRegistry.getActive();
            if (!provider) {
                throw new Error("No AI provider configured");
            }

            // === CHECK FUNCTION CALLING SUPPORT ===
            const supportsFunctionCalling = provider.capabilities.tools;

            if (supportsFunctionCalling) {
                // === FUNCTION CALLING MODE ===
                await this.handleFunctionCallingMode(provider, messages, intent);
            } else {
                // === CONTEXT INJECTION MODE ===
                await this.handleContextInjectionMode(provider, messages);
            }
        } catch (error) {
            console.error("[AIPanel] Error getting AI response:", error);
            throw error;
        } finally {
            this.chatView.hideTypingIndicator();
        }
    }

    /**
     * Handle function calling mode (for OpenAI, Claude, Gemini)
     */
    async handleFunctionCallingMode(provider, messages, intent) {
        // Get available tools
        const allTools = functionCallingService.getAvailableFunctions();

        // Filter tools based on intent (optional optimization)
        let tools = allTools;
        if (intent && intent.suggestedTools.length > 0) {
            // Prioritize suggested tools but include all for flexibility
            const suggestedToolNames = intent.suggestedTools;
            tools = allTools.filter(
                (tool) =>
                    suggestedToolNames.includes(tool.name) ||
                    tool.name.includes("semantic") || // Always include semantic search
                    tool.name.includes("Natural"), // Always include natural query
            );

            if (this.settings.enableDebugLogs) {
                console.log(
                    `[AIPanel] Using ${tools.length} tools (${suggestedToolNames.length} suggested)`,
                );
            }
        }

        // Call AI with tools
        const response = await provider.chat(messages, {
            tools: tools,
            temperature: 0.7,
            max_tokens: 2048,
        });

        // Handle response
        if (response.tool_calls && response.tool_calls.length > 0) {
            // === TOOL CALLS DETECTED ===
            await this.handleToolCalls(response.tool_calls, messages, provider);
        } else {
            // === DIRECT RESPONSE ===
            this.conversationHistory.push({
                role: "assistant",
                content: response.content,
                timestamp: new Date().toISOString(),
            });

            this.chatView.addMessage("assistant", response.content);
        }
    }

    /**
     * Handle tool calls (function execution)
     */
    async handleToolCalls(toolCalls, messages, provider) {
        console.log(`[AIPanel] Processing ${toolCalls.length} tool calls`);

        // Display tool calls in UI
        this.chatView.showToolCalls(toolCalls);

        // Execute each tool
        const toolResults = [];

        for (const toolCall of toolCalls) {
            const {id, function: func} = toolCall;
            const {name, arguments: argsStr} = func;

            try {
                // Parse arguments
                const args =
                    typeof argsStr === "string" ? JSON.parse(argsStr) : argsStr;

                console.log(`[AIPanel] Executing tool: ${name}`, args);

                // Show execution in UI
                this.chatView.showToolExecution(name, args);

                // Execute function
                const result = await functionCallingService.executeFunction(name, args);

                // Show result in UI
                this.chatView.showToolResult(name, result);

                toolResults.push({
                    tool_call_id: id,
                    role: "tool",
                    name: name,
                    content: JSON.stringify(result),
                });
            } catch (error) {
                console.error(`[AIPanel] Error executing tool ${name}:`, error);

                toolResults.push({
                    tool_call_id: id,
                    role: "tool",
                    name: name,
                    content: JSON.stringify({
                        success: false,
                        error: error.message,
                    }),
                });
            }
        }

        // Add assistant message with tool calls
        this.conversationHistory.push({
            role: "assistant",
            content: "",
            tool_calls: toolCalls,
            timestamp: new Date().toISOString(),
        });

        // Add tool results
        this.conversationHistory.push(...toolResults);

        // === GET FINAL RESPONSE ===
        // AI now synthesizes the tool results into a human-readable response
        const finalMessages = [
            ...messages,
            {
                role: "assistant",
                content: "",
                tool_calls: toolCalls,
            },
            ...toolResults,
        ];

        console.log("[AIPanel] Getting final synthesis from AI");
        this.chatView.showSynthesizing();

        const finalResponse = await provider.chat(finalMessages, {
            temperature: 0.7,
            max_tokens: 2048,
        });

        // Display final response
        this.conversationHistory.push({
            role: "assistant",
            content: finalResponse.content,
            timestamp: new Date().toISOString(),
        });

        this.chatView.addMessage("assistant", finalResponse.content);
    }

    /**
     * Handle context injection mode (for models without function calling)
     */
    async handleContextInjectionMode(provider, messages) {
        // This is a fallback for models that don't support function calling
        // The enhanced system prompt already includes guidance on available functions

        console.log(
            "[AIPanel] Using context injection mode (no native function calling)",
        );

        const response = await provider.chat(messages, {
            temperature: 0.7,
            max_tokens: 2048,
        });

        this.conversationHistory.push({
            role: "assistant",
            content: response.content,
            timestamp: new Date().toISOString(),
        });

        this.chatView.addMessage("assistant", response.content);
    }

    /**
     * Update current context
     */
    setCurrentProject(project) {
        this.currentProject = project;
        this.addRecentActivity(`viewed project: ${project.name}`);
        console.log("[AIPanel] Current project set:", project.name);
    }

    setCurrentScreen(screen) {
        this.currentScreen = screen;
        this.addRecentActivity(`viewed screen: ${screen.name}`);
        console.log("[AIPanel] Current screen set:", screen.name);
    }

    addRecentActivity(activity) {
        this.recentActivity.push({
            activity,
            timestamp: new Date().toISOString(),
        });

        // Keep only last 10 activities
        if (this.recentActivity.length > 10) {
            this.recentActivity.shift();
        }
    }

    /**
     * Clear conversation history
     */
    clearConversation() {
        this.conversationHistory = [];
        this.chatView.clear();
        console.log("[AIPanel] Conversation cleared");
    }

    /**
     * Update settings
     */
    updateSettings(settings) {
        this.settings = {
            ...this.settings,
            ...settings,
        };
        console.log("[AIPanel] Settings updated:", this.settings);
    }

    /**
     * Get conversation stats
     */
    getConversationStats() {
        const stats = {
            totalMessages: this.conversationHistory.length,
            userMessages: this.conversationHistory.filter((m) => m.role === "user")
                .length,
            assistantMessages: this.conversationHistory.filter(
                (m) => m.role === "assistant",
            ).length,
            toolCalls: this.conversationHistory.filter((m) => m.tool_calls).length,
            averageIntentConfidence: 0,
        };

        // Calculate average intent confidence
        const intents = this.conversationHistory
            .filter((m) => m.metadata?.intent)
            .map((m) => m.metadata.intent.confidence);

        if (intents.length > 0) {
            stats.averageIntentConfidence =
                intents.reduce((sum, c) => sum + c, 0) / intents.length;
        }

        return stats;
    }

    /**
     * Export conversation
     */
    exportConversation() {
        const exportData = {
            version: "1.0",
            exportedAt: new Date().toISOString(),
            settings: this.settings,
            context: {
                currentProject: this.currentProject,
                currentScreen: this.currentScreen,
                recentActivity: this.recentActivity,
            },
            conversation: this.conversationHistory,
            stats: this.getConversationStats(),
        };

        return JSON.stringify(exportData, null, 2);
    }

    /**
     * Import conversation
     */
    importConversation(jsonString) {
        try {
            const data = JSON.parse(jsonString);

            this.conversationHistory = data.conversation || [];
            this.currentProject = data.context?.currentProject || null;
            this.currentScreen = data.context?.currentScreen || null;
            this.recentActivity = data.context?.recentActivity || [];

            // Rebuild chat view
            this.chatView.clear();
            for (const msg of this.conversationHistory) {
                if (msg.role === "user" || msg.role === "assistant") {
                    this.chatView.addMessage(msg.role, msg.content);
                }
            }

            console.log("[AIPanel] Conversation imported");
            return true;
        } catch (error) {
            console.error("[AIPanel] Error importing conversation:", error);
            return false;
        }
    }
}

export default AIPanel;

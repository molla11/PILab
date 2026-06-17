import { OpenRouter, stepCountIs, type StreamableOutputItem, type Tool } from '@openrouter/agent';
import { EventEmitter } from 'eventemitter3';

export interface Message {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface AgentEvents {
  'message:user': (message: Message) => void;
  'message:assistant': (message: Message) => void;
  'item:update': (item: StreamableOutputItem) => void;
  'stream:start': () => void;
  'stream:delta': (delta: string, accumulated: string) => void;
  'stream:end': (fullText: string) => void;
  'tool:call': (name: string, args: unknown) => void;
  'tool:result': (name: string, result: unknown) => void;
  'reasoning:update': (text: string) => void;
  error: (error: Error) => void;
  'thinking:start': () => void;
  'thinking:end': () => void;
}

export interface AgentConfig {
  apiKey: string;
  model?: string;
  instructions?: string;
  tools?: Tool[];
  maxSteps?: number;
}

type AgentConfigResolved = {
  apiKey: string;
  model: string;
  instructions: string;
  tools: Tool[];
  maxSteps: number;
};

export class Agent extends EventEmitter<AgentEvents> {
  private client: unknown;
  private messages: Message[] = [];
  private config: AgentConfigResolved;

  constructor(config: AgentConfig) {
    super();
    this.config = {
      apiKey: config.apiKey,
      model: config.model ?? 'openrouter/auto',
      instructions: config.instructions ?? '당신은 사용자를 돕는 한국어 assistant입니다.',
      tools: config.tools ?? [],
      maxSteps: config.maxSteps ?? 5
    };
  }

  getMessages(): Message[] {
    return [...this.messages];
  }

  clearHistory(): void {
    this.messages = [];
  }

  setInstructions(instructions: string): void {
    this.config.instructions = instructions;
  }

  addTool(newTool: Tool): void {
    this.config.tools.push(newTool);
  }

  async send(content: string): Promise<string> {
    const userMessage: Message = { role: 'user', content };
    this.messages.push(userMessage);
    this.emit('message:user', userMessage);
    this.emit('thinking:start');

    try {
      const client = await this.getClient();
      const result = client.callModel({
        model: this.config.model,
        instructions: this.config.instructions,
        input: content,
        tools: this.config.tools.length > 0 ? this.config.tools : undefined,
        stopWhen: [stepCountIs(this.config.maxSteps)]
      });

      this.emit('stream:start');
      let fullText = '';

      for await (const item of result.getItemsStream()) {
        this.emit('item:update', item);
        switch (item.type) {
          case 'message': {
            const textContent = item.content?.find((contentItem: { type: string }) => contentItem.type === 'output_text');
            if (textContent && 'text' in textContent && textContent.text !== fullText) {
              const delta = textContent.text.slice(fullText.length);
              fullText = textContent.text;
              this.emit('stream:delta', delta, fullText);
            }
            break;
          }
          case 'function_call':
            if (item.status === 'completed') {
              this.emit('tool:call', item.name, safeParseJson(item.arguments ?? '{}'));
            }
            break;
          case 'function_call_output':
            this.emit('tool:result', item.callId, item.output);
            break;
          case 'reasoning': {
            const reasoningText = item.content?.find((contentItem: { type: string }) => contentItem.type === 'reasoning_text');
            if (reasoningText && 'text' in reasoningText) {
              this.emit('reasoning:update', reasoningText.text);
            }
            break;
          }
        }
      }

      if (!fullText) {
        fullText = await result.getText();
      }

      this.emit('stream:end', fullText);
      const assistantMessage: Message = { role: 'assistant', content: fullText };
      this.messages.push(assistantMessage);
      this.emit('message:assistant', assistantMessage);
      return fullText;
    } catch (error) {
      const normalized = error instanceof Error ? error : new Error(String(error));
      this.emit('error', normalized);
      throw normalized;
    } finally {
      this.emit('thinking:end');
    }
  }

  async sendSync(content: string): Promise<string> {
    const userMessage: Message = { role: 'user', content };
    this.messages.push(userMessage);
    this.emit('message:user', userMessage);

    try {
      const client = await this.getClient();
      const result = client.callModel({
        model: this.config.model,
        instructions: this.config.instructions,
        input: content,
        tools: this.config.tools.length > 0 ? this.config.tools : undefined,
        stopWhen: [stepCountIs(this.config.maxSteps)]
      });
      const fullText = await result.getText();
      const assistantMessage: Message = { role: 'assistant', content: fullText };
      this.messages.push(assistantMessage);
      this.emit('message:assistant', assistantMessage);
      return fullText;
    } catch (error) {
      const normalized = error instanceof Error ? error : new Error(String(error));
      this.emit('error', normalized);
      throw normalized;
    }
  }

  private async getClient(): Promise<OpenRouterClient> {
    if (!this.client) {
      this.client = new OpenRouter({ apiKey: this.config.apiKey });
    }
    return this.client as OpenRouterClient;
  }
}

export function createAgent(config: AgentConfig): Agent {
  return new Agent(config);
}

function safeParseJson(value: string): unknown {
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
}

type OpenRouterClient = InstanceType<typeof OpenRouter>;

import { EventEmitter } from 'eventemitter3';

type StreamableOutputItem = unknown;
type Tool = unknown;

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
      this.emit('stream:start');
      const fullText = await this.callModel(content);
      this.emit('stream:delta', fullText, fullText);
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
      const fullText = await this.callModel(content);
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

  private async callModel(content: string): Promise<string> {
    const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${this.config.apiKey}`,
        'Content-Type': 'application/json',
        'HTTP-Referer': 'https://api.molla.kr',
        'X-Title': 'PILab'
      },
      body: JSON.stringify({
        model: this.config.model,
        messages: [
          { role: 'system', content: this.config.instructions },
          { role: 'user', content }
        ]
      })
    });

    const raw = await response.text();
    if (!response.ok) {
      throw new Error(`OpenRouter HTTP ${response.status}: ${redactSecrets(raw, this.config.apiKey)}`);
    }

    const parsed = safeParseJson(raw) as OpenRouterChatCompletionResponse;
    const message = parsed.choices?.[0]?.message;
    const text = message?.content;
    if (typeof text === 'string' && text.trim().length > 0) {
      return text;
    }

    throw new Error(`OpenRouter response did not contain output text: ${redactSecrets(raw, this.config.apiKey).slice(0, 1000)}`);
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

type OpenRouterChatCompletionResponse = {
  choices?: Array<{
    message?: {
      content?: string | null;
    };
  }>;
};

function redactSecrets(value: string, apiKey: string): string {
  return value
    .replaceAll(apiKey, '[redacted]')
    .replace(/sk-or-v1-[A-Za-z0-9]+/g, '[redacted]');
}

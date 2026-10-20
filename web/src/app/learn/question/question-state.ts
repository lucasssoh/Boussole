import { WritableSignal, computed, signal } from '@angular/core';
import { BoussoleApi } from '../../core/boussole-api';
import {
  AttemptRequest,
  AttemptResult,
  BlockView,
  CheckedItem,
  QuestionHints,
  QuestionProgress,
  RevealedSolution,
} from '../../core/api';
import { SessionRun } from '../session-run';

/**
 * What every question does, MCQ or exercise: send an attempt, reveal the next hint, show the
 * solution once allowed. Each question component owns one, and only says how its response is built.
 */
export class QuestionState {
  readonly progress: WritableSignal<QuestionProgress>;
  readonly result = signal<AttemptResult | null>(null);
  readonly hints: WritableSignal<BlockView[][]>;
  readonly hintTotal: number;
  readonly solution = signal<RevealedSolution | null>(null);
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);

  readonly hintsLeft = computed(() => this.hints().length < this.hintTotal);
  readonly solutionAllowed = computed(() => this.freeSolution || this.progress().attempts > 0);

  /** Starts from what the server knows: attempts made, hints already revealed. */
  constructor(
    private readonly api: BoussoleApi,
    private readonly subject: string,
    readonly id: string,
    progress: QuestionProgress,
    hints: QuestionHints,
    private readonly run: SessionRun | null,
    private readonly freeSolution = false,
  ) {
    this.progress = signal(progress);
    this.hints = signal(hints.revealed);
    this.hintTotal = hints.total;
  }

  item(key: string | number): CheckedItem | undefined {
    return this.result()?.items.find((item) => item.key === String(key));
  }

  async attempt(request: AttemptRequest): Promise<void> {
    await this.call(async () => {
      const result = await this.api.attempt(this.subject, this.id, request);
      this.result.set(result);
      this.progress.set(result.progress);
      this.run?.record(this.id, result.verdict);
    });
  }

  async nextHint(): Promise<void> {
    await this.call(async () => {
      const hint = await this.api.nextHint(this.subject, this.id);
      this.hints.update((hints) => [...hints, hint.blocks]);
    });
  }

  async showSolution(): Promise<void> {
    await this.call(async () => this.solution.set(await this.api.solution(this.subject, this.id)));
  }

  private async call(work: () => Promise<void>): Promise<void> {
    this.busy.set(true);
    this.error.set(null);
    try {
      await work();
    } catch (error) {
      this.error.set(messageOf(error));
    } finally {
      this.busy.set(false);
    }
  }
}

function messageOf(error: unknown): string {
  const body = (error as { error?: { message?: string } })?.error;
  return body?.message ?? (error as Error)?.message ?? String(error);
}

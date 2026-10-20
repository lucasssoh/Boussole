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

  /** The response as the learner is writing it, kept with the question rather than its component. */
  readonly picked = signal<ReadonlySet<number>>(new Set());
  readonly value = signal('');
  readonly order: WritableSignal<string[]>;
  readonly pairs = signal<Readonly<Record<string, string>>>({});
  readonly gaps = signal<Readonly<Record<number, string>>>({});
  readonly error = signal<string | null>(null);

  /** The learner answers once; the question is then closed for this sitting. */
  readonly answered = computed(() => this.result() !== null);
  readonly hintsLeft = computed(
    () => !this.answered() && this.hints().length < this.hintTotal && (this.run?.hintsAllowed() ?? true),
  );
  /** The correction shows right after the answer, except in an exam, where it waits for the end. */
  readonly correctionShown = computed(
    () => this.solution() !== null && (this.isFree || (this.answered() && (this.run?.correctionsOpen() ?? true))),
  );

  /** Starts from what the server knows: attempts made, hints already revealed. */
  constructor(
    private readonly api: BoussoleApi,
    private readonly subject: string,
    private readonly session: string | null,
    readonly id: string,
    progress: QuestionProgress,
    hints: QuestionHints,
    private readonly run: SessionRun | null,
    readonly isFree = false,
    shuffledItems: string[] = [],
  ) {
    this.order = signal([...shuffledItems]);
    this.progress = signal(progress);
    this.hints = signal(hints.revealed);
    this.hintTotal = hints.total;
  }

  item(key: string | number): CheckedItem | undefined {
    return this.result()?.items.find((item) => item.key === String(key));
  }

  async attempt(request: AttemptRequest): Promise<void> {
    await this.call(async () => {
      const result = await this.api.attempt(this.subject, this.id, request, this.session);
      this.result.set(result);
      this.solution.set(result.solution);
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

  /** A free exercise is compared with its solution before the learner grades it. */
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

import { WritableSignal, computed, signal, untracked } from '@angular/core';
import { BoussoleApi } from '../../core/boussole-api';
import {
  AttemptRequest,
  AttemptResult,
  BlockView,
  CheckedItem,
  QuestionHints,
  QuestionProgress,
  RevealedSolution,
  Verdict,
} from '../../core/api';
import { storeValue, storedText } from '../../core/local-preferences';
import { StatsService } from '../../core/stats.service';
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

  /**
   * The verdict of an answer given in an earlier sitting of this lesson, when this browser did not
   * keep the answer itself: the question is closed and only its verdict and solution show.
   */
  private readonly earlierVerdict = signal<Verdict | null>(null);
  readonly verdict = computed(() => this.result()?.verdict ?? this.earlierVerdict());

  /** The learner answers once: for this sitting, and for good in a lesson. */
  readonly answered = computed(() => this.verdict() !== null);
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
    private readonly stats: StatsService | null = null,
    /** The version of the track the question is read in: its answer, hints and solution. */
    private readonly language: string | null = null,
  ) {
    this.order = signal([...shuffledItems]);
    this.progress = signal(progress);
    this.hints = signal(hints.revealed);
    this.hintTotal = hints.total;
    const earlier = progress.answeredHere;
    if (earlier) {
      // A question is created while a page computes what it shows, where signals cannot be written.
      untracked(() => this.restore(earlier));
    }
  }

  item(key: string | number): CheckedItem | undefined {
    return this.result()?.items.find((item) => item.key === String(key));
  }

  async attempt(request: AttemptRequest): Promise<void> {
    await this.call(async () => {
      const result = await this.api.attempt(this.subject, this.id, request, this.session, this.language);
      this.result.set(result);
      this.solution.set(result.solution);
      this.progress.set(result.progress);
      if (this.run?.kind() === 'lesson') {
        storeValue(this.keptKey(), JSON.stringify({ request, result } satisfies KeptAnswer));
      }
      this.run?.record(this.id, result.verdict, result.xpGained);
      this.stats?.refresh();
    });
  }

  async nextHint(): Promise<void> {
    await this.call(async () => {
      const hint = await this.api.nextHint(this.subject, this.id, this.language);
      this.hints.update((hints) => [...hints, hint.blocks]);
    });
  }

  /** A free exercise is compared with its solution before the learner grades it. */
  async showSolution(): Promise<void> {
    await this.call(async () => this.solution.set(await this.api.solution(this.subject, this.id, this.language)));
  }

  /**
   * Shows again the answer given in an earlier sitting: as it was, when this browser kept it,
   * else its verdict with the solution.
   */
  private restore(verdict: Verdict): void {
    const kept = keptAnswer(storedText(this.keptKey()));
    if (kept && kept.result.verdict === verdict) {
      this.fillFrom(kept.request);
      this.result.set(kept.result);
      this.solution.set(kept.result.solution);
      return;
    }
    this.earlierVerdict.set(verdict);
    void this.call(async () => this.solution.set(await this.api.solution(this.subject, this.id, this.language)));
  }

  private fillFrom(request: AttemptRequest): void {
    switch (request.kind) {
      case 'choices':
        this.picked.set(new Set(request.picked));
        break;
      case 'value':
        this.value.set(request.value);
        break;
      case 'gaps':
        this.gaps.set(request.values);
        break;
      case 'order':
        this.order.set([...request.items]);
        break;
      case 'match':
        this.pairs.set(request.pairs);
        break;
      case 'selfGrade':
        break;
    }
  }

  private keptKey(): string {
    return `answer.${this.subject}.${this.session}.${this.id}`;
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

/** An answer given in a lesson, kept in this browser to show it again with its correction. */
interface KeptAnswer {
  request: AttemptRequest;
  result: AttemptResult;
}

function keptAnswer(text: string): KeptAnswer | null {
  try {
    const kept = JSON.parse(text) as KeptAnswer;
    return kept?.request?.kind && kept?.result?.verdict ? kept : null;
  } catch {
    return null;
  }
}

function messageOf(error: unknown): string {
  const body = (error as { error?: { message?: string } })?.error;
  return body?.message ?? (error as Error)?.message ?? String(error);
}

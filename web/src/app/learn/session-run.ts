import { Injectable, OnDestroy, computed, signal } from '@angular/core';
import { SessionKind, Verdict } from '../core/api';
import { QuestionState } from './question/question-state';

/**
 * One sitting on a session, for the session page only: the time actually spent (while the page
 * is visible), the countdown of a timed session, the first verdict on each question, and the
 * state of each question, so that it is found as left when the learner comes back to its page.
 */
@Injectable()
export class SessionRun implements OnDestroy {
  private readonly firstVerdicts = signal<ReadonlyMap<string, Verdict>>(new Map());
  private readonly spent = signal(0);
  private readonly timer = setInterval(() => this.tick(), 1000);
  private readonly questions = new Map<string, QuestionState>();

  readonly kind = signal<SessionKind>('lesson');
  readonly started = signal(false);
  /** The learner ended the session (in an exam, before the time is up). */
  readonly ended = signal(false);
  readonly allowedSeconds = signal<number | null>(null);
  readonly spentSeconds = this.spent.asReadonly();
  readonly remainingSeconds = computed(() => {
    const allowed = this.allowedSeconds();
    return allowed === null ? null : Math.max(0, allowed - this.spent());
  });
  readonly timeIsUp = computed(() => this.remainingSeconds() === 0);
  readonly answered = computed(() => this.firstVerdicts().size);
  readonly verdicts = this.firstVerdicts.asReadonly();
  /** The experience earned in this sitting. */
  readonly xpEarned = signal(0);
  /** The screens seen in this sitting, by index. */
  readonly seen = signal<ReadonlySet<number>>(new Set());
  /** An exam shows its corrections when the time is up or the learner ends it; other sessions, after each answer. */
  readonly correctionsOpen = computed(() => this.kind() !== 'exam' || this.timeIsUp() || this.ended());
  /** An exam gives no hint. */
  readonly hintsAllowed = computed(() => this.kind() !== 'exam');
  readonly rightFirstTime = computed(() => [...this.firstVerdicts().values()].filter((verdict) => verdict === 'correct').length);

  /** A lesson starts at once; a review or an exam when the learner says so, with its countdown. */
  begin(kind: SessionKind, minutes: number): void {
    this.kind.set(kind);
    this.spent.set(0);
    this.firstVerdicts.set(new Map());
    this.questions.clear();
    this.seen.set(new Set());
    this.xpEarned.set(0);
    this.ended.set(false);
    this.allowedSeconds.set(kind === 'lesson' ? null : minutes * 60);
    this.started.set(kind === 'lesson');
  }

  see(screen: number): void {
    if (!this.seen().has(screen)) {
      this.seen.set(new Set(this.seen()).add(screen));
    }
  }

  end(): void {
    this.ended.set(true);
  }

  start(): void {
    this.started.set(true);
  }

  /** The state of a question in this sitting, created the first time its page shows. */
  question(id: string, create: () => QuestionState): QuestionState {
    let state = this.questions.get(id);
    if (!state) {
      state = create();
      this.questions.set(id, state);
    }
    return state;
  }

  record(question: string, verdict: Verdict, xp: number): void {
    this.xpEarned.update((total) => total + xp);
    if (!this.firstVerdicts().has(question)) {
      this.firstVerdicts.set(new Map(this.firstVerdicts()).set(question, verdict));
    }
  }

  earn(xp: number): void {
    this.xpEarned.update((total) => total + xp);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  private tick(): void {
    if (this.started() && document.visibilityState === 'visible') {
      this.spent.update((seconds) => seconds + 1);
    }
  }
}

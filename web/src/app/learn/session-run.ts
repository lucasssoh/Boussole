import { Injectable, OnDestroy, computed, signal } from '@angular/core';
import { Verdict } from '../core/api';
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

  readonly started = signal(false);
  readonly allowedSeconds = signal<number | null>(null);
  readonly spentSeconds = this.spent.asReadonly();
  readonly remainingSeconds = computed(() => {
    const allowed = this.allowedSeconds();
    return allowed === null ? null : Math.max(0, allowed - this.spent());
  });
  readonly timeIsUp = computed(() => this.remainingSeconds() === 0);
  readonly answered = computed(() => this.firstVerdicts().size);
  readonly rightFirstTime = computed(() => [...this.firstVerdicts().values()].filter((verdict) => verdict === 'correct').length);

  /** A lesson starts at once; a timed session when the learner says so. */
  begin(timedMinutes: number | null): void {
    this.spent.set(0);
    this.firstVerdicts.set(new Map());
    this.questions.clear();
    this.allowedSeconds.set(timedMinutes === null ? null : timedMinutes * 60);
    this.started.set(timedMinutes === null);
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

  record(question: string, verdict: Verdict): void {
    if (!this.firstVerdicts().has(question)) {
      this.firstVerdicts.set(new Map(this.firstVerdicts()).set(question, verdict));
    }
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

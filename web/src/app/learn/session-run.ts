import { Injectable, OnDestroy, computed, signal } from '@angular/core';
import { Verdict } from '../core/api';

/**
 * One sitting on a session, for the session page only: the time actually spent (while the page
 * is visible), the countdown of a timed session, and the first verdict on each question.
 */
@Injectable()
export class SessionRun implements OnDestroy {
  private readonly firstVerdicts = signal<ReadonlyMap<string, Verdict>>(new Map());
  private readonly spent = signal(0);
  private readonly timer = setInterval(() => this.tick(), 1000);

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
    this.allowedSeconds.set(timedMinutes === null ? null : timedMinutes * 60);
    this.started.set(timedMinutes === null);
  }

  start(): void {
    this.started.set(true);
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

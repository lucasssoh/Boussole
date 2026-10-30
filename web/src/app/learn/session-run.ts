import { Injectable, OnDestroy, computed, signal, untracked } from '@angular/core';
import { SessionKind, Verdict } from '../core/api';
import { storeValue, storedText } from '../core/local-preferences';
import { QuestionState } from './question/question-state';

/**
 * One sitting on a session, for the session page only: the time actually spent (while the page
 * is visible), the countdown of a timed session, the first answer to each question, and the
 * state of each question, so that it is found as left when the learner comes back to its page.
 */
@Injectable()
export class SessionRun implements OnDestroy {
  private readonly firstAnswers = signal<ReadonlyMap<string, FirstAnswer>>(new Map());
  private readonly firstVerdicts = computed(
    () => new Map([...this.firstAnswers()].map(([question, answer]) => [question, answer.verdict])) as ReadonlyMap<string, Verdict>,
  );
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
  /** The questions that are hooks, guesses of the warmup: they count in no score. */
  private readonly hooks = signal<ReadonlySet<string>>(new Set());
  /** The questions of other sessions asked again here, and the points each question earned in this sitting. */
  private readonly again = signal<ReadonlySet<string>>(new Set());
  private readonly earnedBy = signal<ReadonlyMap<string, number>>(new Map());
  private readonly scoredVerdicts = computed(() => [...this.firstVerdicts()].filter(([question]) => !this.hooks().has(question)));
  readonly answered = computed(() => this.scoredVerdicts().length);
  readonly verdicts = this.firstVerdicts;
  /** The stars of the first answer to each question, when known. */
  readonly stars = computed(
    () => new Map([...this.firstAnswers()].flatMap(([question, answer]) => (answer.stars === null ? [] : [[question, answer.stars] as const]))) as ReadonlyMap<string, number>,
  );
  /** The question answered last in this sitting: its stars arrive on the Trail. */
  readonly lastAnswered = signal<string | null>(null);
  /** The points earned in this sitting, and the bonus of a session without a mistake. */
  readonly pointsEarned = signal(0);
  readonly bonus = signal(0);
  /** The screens seen, by index: in this sitting, and in earlier ones on this browser. */
  readonly seen = signal<ReadonlySet<number>>(new Set());
  private seenKey: string | null = null;
  /** An exam shows its corrections when the time is up or the learner ends it; other sessions, after each answer. */
  readonly correctionsOpen = computed(() => this.kind() !== 'exam' || this.timeIsUp() || this.ended());
  /** An exam gives no hint. */
  readonly hintsAllowed = computed(() => this.kind() !== 'exam');
  readonly rightFirstTime = computed(() => this.scoredVerdicts().filter(([, verdict]) => verdict === 'correct').length);
  /** The wrap-up: the session's own questions, those asked again, and those missed. */
  readonly tally = computed(() => {
    const own = { right: 0, answered: 0, points: 0 };
    const again = { right: 0, answered: 0, points: 0 };
    let missed = 0;
    for (const [question, verdict] of this.scoredVerdicts()) {
      const line = this.again().has(question) ? again : own;
      line.answered++;
      line.points += this.earnedBy().get(question) ?? 0;
      if (verdict === 'correct') {
        line.right++;
      } else {
        missed++;
      }
    }
    return { own, again, missed };
  });

  /**
   * A lesson starts at once; a review or an exam when the learner says so, with its countdown.
   * {@code answered}: the questions of a lesson answered in an earlier sitting, whose verdict stays.
   */
  begin(kind: SessionKind, minutes: number, subject: string, session: string, answered: ReadonlyMap<string, FirstAnswer> = new Map()): void {
    this.seenKey = `seen.${subject}.${session}`;
    this.kind.set(kind);
    this.spent.set(0);
    this.firstAnswers.set(new Map(answered));
    this.lastAnswered.set(null);
    this.questions.clear();
    this.seen.set(new Set(storedNumbers(this.seenKey)));
    this.pointsEarned.set(0);
    this.bonus.set(0);
    this.hooks.set(new Set());
    this.again.set(new Set());
    this.earnedBy.set(new Map());
    this.ended.set(false);
    this.allowedSeconds.set(kind === 'lesson' ? null : minutes * 60);
    this.started.set(kind === 'lesson');
  }

  /**
   * The session was opened again in another language: its questions start afresh from what the
   * server knows, while the time spent and the verdicts of this sitting stay.
   */
  reloadQuestions(answered: ReadonlyMap<string, FirstAnswer>): void {
    this.questions.clear();
    this.firstAnswers.set(new Map([...answered, ...this.firstAnswers()]));
  }

  see(screen: number): void {
    if (!this.seen().has(screen)) {
      this.seen.set(new Set(this.seen()).add(screen));
      if (this.seenKey) {
        storeValue(this.seenKey, [...this.seen()].join(','));
      }
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
      const created = create();
      state = created;
      // Called from a computed, which may not write signals: the marks are written outside it.
      untracked(() => {
        if (created.hook) {
          this.hooks.set(new Set(this.hooks()).add(id));
        }
        if (created.reused) {
          this.again.set(new Set(this.again()).add(id));
        }
      });
      this.questions.set(id, state);
    }
    return state;
  }

  record(question: string, verdict: Verdict, points: number, stars: number): void {
    this.pointsEarned.update((total) => total + points);
    this.earnedBy.set(new Map(this.earnedBy()).set(question, (this.earnedBy().get(question) ?? 0) + points));
    if (!this.firstAnswers().has(question)) {
      this.firstAnswers.set(new Map(this.firstAnswers()).set(question, { verdict, stars }));
      this.lastAnswered.set(question);
    }
  }

  /** The bonus of the session, once finished. */
  earnBonus(points: number): void {
    this.bonus.set(points);
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

/** The first answer to a question in a session: its verdict, and its stars when the server gave them. */
export interface FirstAnswer {
  verdict: Verdict;
  stars: number | null;
}

function storedNumbers(key: string): number[] {
  return storedText(key)
    .split(',')
    .map(Number)
    .filter((value) => Number.isInteger(value) && value >= 0);
}

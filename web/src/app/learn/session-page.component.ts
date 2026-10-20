import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { storedValue, storeValue } from '../core/local-preferences';
import { ServerSettingsService } from '../core/server-settings.service';
import { StatsService } from '../core/stats.service';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { BlockListComponent } from './blocks/block-list.component';
import { clock } from './blocks/video-block.component';
import { LearnFacade } from './learn.facade';
import { SessionPathComponent } from './session-path.component';
import { BlockView } from '../core/api';
import { SessionScreen, sessionPlan } from './session-screens';
import { SessionRun } from './session-run';
import { SidePanelComponent } from './side-panel.component';

/**
 * The session player: one screen at a time (the goal, then each explanation, example and
 * question, then the end), the plan of the session beside it, and the video or document opened.
 * The screen is kept in the address, and in this browser for the next visit. A lesson ends when
 * the learner says so; a review or a mock exam is timed.
 */
@Component({
  selector: 'app-session-page',
  imports: [RichTextComponent, RouterLink, TranslocoPipe, BlockListComponent, SessionPathComponent, SidePanelComponent],
  providers: [SessionRun],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'player subject-scope',
    '[class.with-panel]': 'learn.panel() !== null',
    '[class.outline-open]': 'outlineOpen()',
    '(document:keydown)': 'onKey($event)',
    '[style.--subject]': 'color()',
  },
  template: `
    @if (opened(); as current) {
      <app-session-path
        [session]="current"
        [plan]="plan()"
        [current]="index()"
        [reachable]="gate()"
        [attr.lang]="learn.course()?.lang"
        (go)="goTo($event); outlineOpen.set(false)"
      />
      @if (outlineOpen()) {
        <button type="button" class="outline-backdrop" [attr.aria-label]="'panel.close' | transloco" (click)="outlineOpen.set(false)"></button>
      }

      <main class="session" [attr.lang]="learn.course()?.lang">
        <div class="screen-bar">
          <button type="button" class="quiet outline-toggle" (click)="outlineOpen.set(!outlineOpen())">☰ {{ 'session.plan' | transloco }}</button>
          <span class="screen-where"><app-rich-text [text]="where()" /></span>
          @if (timed() && run.started()) {
            <span class="timer" [class.over]="run.timeIsUp()" role="timer">⏱ {{ remaining() }}</span>
          }
          <span class="screen-count">{{ index() + 1 }} / {{ plan().screens.length }}</span>
          <progress [max]="plan().screens.length - 1" [value]="index()"></progress>
        </div>

        @for (shown of [screen()]; track index()) {
        <article [class]="'screen screen-' + shown.kind">
          @switch (shown.kind) {
            @case ('start') {
              <p class="eyebrow">
                {{ current.chapterTitle }} ·
                <span class="tag" [class]="current.kind">{{ 'kind.' + current.kind | transloco }}</span>
                {{ (timed() ? 'course.timed' : 'course.estimate') | transloco: { minutes: current.duration } }}
              </p>
              <h1><app-rich-text [text]="current.title" /></h1>
              <p class="lead"><app-rich-text [text]="current.goal" /></p>
              @if (current.objectives.length > 0) {
                <div class="objectives">
                  <h2>{{ 'session.objectives' | transloco }}</h2>
                  <ul>
                    @for (objective of current.objectives; track $index) {
                      <li><app-rich-text [text]="objective" /></li>
                    }
                  </ul>
                </div>
              }
              @if (timed()) {
                <p class="note">{{ 'timed.intro' | transloco: { minutes: current.duration, questions: roundedQuestionMinutes() } }}</p>
              }
            }
            @case ('end') {
              <h1>{{ 'session.endTitle' | transloco }}</h1>
              <div class="bilan-stats">
                <div class="bilan-stat xp">
                  <strong>+{{ run.xpEarned() }} XP</strong>
                  <span>{{ 'session.xpEarned' | transloco }}</span>
                </div>
                <div class="bilan-stat right">
                  <strong>{{ run.rightFirstTime() }} / {{ run.answered() }}</strong>
                  <span>{{ 'session.firstTime' | transloco }}</span>
                </div>
                @if (stats.stats(); as current) {
                  <div class="bilan-stat streak">
                    <strong>{{ current.streakDays }}</strong>
                    <span>{{ 'stats.streak' | transloco }}</span>
                  </div>
                }
              </div>
              @if (finishedIn() === null) {
                <button type="button" class="primary" (click)="finish()">
                  {{ (current.kind === 'exam' ? 'session.endExam' : 'session.finish') | transloco }}
                </button>
              } @else {
                <p class="verdict correct">{{ 'session.finished' | transloco: { minutes: finishedIn() } }}</p>
              }
              <nav class="session-nav">
                <a [routerLink]="['/subjects', current.subject]">← {{ learn.course()?.name }}</a>
                @if (current.next) {
                  <a class="next" [routerLink]="['/subjects', current.subject, 'sessions', current.next]">{{ 'session.next' | transloco }} →</a>
                }
              </nav>
            }
            @default {
              @if (timed() && !run.started()) {
                <p class="frame warning">{{ 'timed.notStarted' | transloco }}</p>
              } @else {
                <app-block-list [blocks]="shown.blocks" />
              }
            }
          }
        </article>
        }

        <nav class="screen-nav">
          @if (index() > 0) {
            <button type="button" (click)="goTo(index() - 1)">← {{ 'session.previousScreen' | transloco }}</button>
          }
          @if (screen().kind === 'start' && timed() && !run.started()) {
            <button type="button" class="primary" (click)="run.start(); goTo(1)">{{ 'timed.start' | transloco }} →</button>
          } @else if (index() < plan().screens.length - 1) {
            @if (index() >= gate()) {
              <span class="gate-note">{{ 'session.answerFirst' | transloco }}</span>
            }
            <button type="button" class="primary" [disabled]="index() >= gate()" (click)="goTo(index() + 1)">
              {{ (screen().kind === 'start' ? 'session.begin' : 'session.nextScreen') | transloco }} →
            </button>
          }
        </nav>
      </main>

      @if (learn.panel()) {
        <app-side-panel />
      }
    } @else if (learn.unreachable()) {
      <main class="session"><p class="frame warning">{{ 'app.unreachable' | transloco }}</p></main>
    }
  `,
})
export class SessionPageComponent {
  readonly subject = input.required<string>();
  readonly session = input.required<string>();
  /** The screen shown, from 1, kept in the address. */
  readonly screenParam = input<string | undefined>(undefined, { alias: 'screen' });

  protected readonly learn = inject(LearnFacade);
  protected readonly run = inject(SessionRun);
  private readonly router = inject(Router);
  private readonly settings = inject(ServerSettingsService);
  protected readonly stats = inject(StatsService);

  protected readonly finishedIn = signal<number | null>(null);
  protected readonly outlineOpen = signal(false);
  protected readonly opened = computed(() => {
    const session = this.learn.session();
    return session && session.subject === this.subject() && session.id === this.session() ? session : null;
  });
  protected readonly plan = computed(() => sessionPlan(this.opened()?.parts ?? []));
  protected readonly index = computed(() => {
    const wanted = Number(this.screenParam() ?? 1) - 1;
    return Number.isInteger(wanted) ? Math.min(Math.max(wanted, 0), this.gate()) : 0;
  });
  protected readonly screen = computed<SessionScreen>(() => this.plan().screens[this.index()]);
  protected readonly where = computed(() => {
    const screen = this.screen();
    const part = this.plan().parts[screen.part]?.part;
    return part?.kind === 'notion' ? (part.title ?? '') : (this.opened()?.title ?? '');
  });
  protected readonly timed = computed(() => this.opened()?.kind !== 'lesson');
  protected readonly color = computed(() => subjectColor(this.subject()));
  /**
   * The last screen the learner may reach: in a lesson, the first question not answered yet in
   * this sitting, since the correction is part of the lesson. Practice exercises, free exercises
   * and timed sessions never hold the learner, nor does free_navigation in config.yaml.
   */
  protected readonly gate = computed(() => {
    const screens = this.plan().screens;
    if (this.opened()?.kind !== 'lesson' || this.settings.freeNavigation()) {
      return screens.length - 1;
    }
    const verdicts = this.run.verdicts();
    const blocking = screens.findIndex((screen) => holds(screen, this.plan().parts[screen.part]?.part.kind, this.exerciseType(screen))
      && !verdicts.has(screen.question!.id) && !this.answeredBefore(screen));
    return blocking < 0 ? screens.length - 1 : blocking;
  });
  protected readonly roundedQuestionMinutes = computed(() => Math.round(this.opened()?.questionMinutes ?? 0));
  protected readonly remaining = computed(() => clock(this.run.remainingSeconds() ?? 0));

  constructor() {
    effect(() => {
      const subject = this.subject();
      const session = this.session();
      untracked(() => {
        this.finishedIn.set(null);
        void this.learn.openSession(subject, session).then(
          () => {
            const opened = this.learn.session();
            if (opened) {
              this.run.begin(opened.kind, opened.duration, opened.subject, opened.id);
              this.resumeStoredScreen(opened.kind === 'lesson');
            }
          },
          () => undefined,
        );
      });
    });
    effect(() => {
      const wanted = Number(this.screenParam() ?? 1) - 1;
      if (this.opened() && wanted > this.index()) {
        untracked(() => this.goTo(this.index()));
      }
    });
    effect(() => {
      const index = this.index();
      const session = this.opened();
      if (session) {
        untracked(() => this.run.see(index));
        storeValue(positionKey(session.subject, session.id), String(index + 1));
      }
    });
  }

  protected goTo(index: number): void {
    if (index > this.gate()) {
      return;
    }
    void this.router.navigate([], { queryParams: { screen: index + 1 }, replaceUrl: true });
    scrollTo({ top: 0 });
  }

  /** → and ← change screen, unless the learner is typing. */
  protected onKey(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    if (event.altKey || event.ctrlKey || event.metaKey || target.closest('input, textarea, select, [contenteditable]')) {
      return;
    }
    if (event.key === 'ArrowRight' && this.index() < this.gate() && !(this.timed() && !this.run.started())) {
      this.goTo(this.index() + 1);
    } else if (event.key === 'ArrowLeft' && this.index() > 0) {
      this.goTo(this.index() - 1);
    }
  }

  protected async finish(): Promise<void> {
    const seconds = this.run.spentSeconds();
    this.run.end();
    this.run.earn(await this.learn.finishSession(seconds));
    this.stats.refresh();
    this.finishedIn.set(Math.max(1, Math.round(seconds / 60)));
  }

  private exerciseType(screen: SessionScreen): string | undefined {
    const block = this.questionOf(screen);
    return block?.type === 'exercise' ? block.exerciseType : undefined;
  }

  /** A question answered in an earlier sitting no longer holds the learner. */
  private answeredBefore(screen: SessionScreen): boolean {
    const block = this.questionOf(screen);
    return block?.type === 'mcq' || block?.type === 'exercise' ? block.progress.attempts > 0 : false;
  }

  private questionOf(screen: SessionScreen): BlockView | undefined {
    return screen.blocks.find((found) => (found.type === 'exercise' || found.type === 'mcq') && found.id === screen.question?.id);
  }

  /** Coming back to a lesson without a screen in the address, the learner finds the screen they left. */
  private resumeStoredScreen(allowed: boolean): void {
    const session = this.opened();
    if (!allowed || !session || this.screenParam()) {
      return;
    }
    const stored = Number(storedValue(positionKey(session.subject, session.id), screenNumbers(this.plan().screens.length), '1'));
    if (stored > 1) {
      this.goTo(stored - 1);
    }
  }
}

/** A question screen holds the learner until answered, unless it is practice or a free exercise. */
function holds(screen: SessionScreen, part: string | undefined, exerciseType: string | undefined): boolean {
  return screen.kind === 'question' && part !== 'practice' && exerciseType !== 'free';
}

function positionKey(subject: string, session: string): string {
  return `screen.${subject}.${session}`;
}

function screenNumbers(count: number): string[] {
  return Array.from({ length: count }, (_, index) => String(index + 1));
}

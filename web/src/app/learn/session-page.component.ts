import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { storedValue, storeValue } from '../core/local-preferences';
import { RichTextComponent } from '../shared/rich-text.component';
import { BlockListComponent } from './blocks/block-list.component';
import { clock } from './blocks/video-block.component';
import { LearnFacade } from './learn.facade';
import { SessionOutlineComponent } from './session-outline.component';
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
  imports: [RichTextComponent, RouterLink, TranslocoPipe, BlockListComponent, SessionOutlineComponent, SidePanelComponent],
  providers: [SessionRun],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'player',
    '[class.with-panel]': 'learn.panel() !== null',
    '[class.outline-open]': 'outlineOpen()',
    '(document:keydown)': 'onKey($event)',
  },
  template: `
    @if (opened(); as current) {
      <app-session-outline
        [session]="current"
        [plan]="plan()"
        [current]="index()"
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

        <article [class]="'screen screen-' + screen().kind">
          @switch (screen().kind) {
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
              @if (run.answered() > 0) {
                <p class="score">{{ 'timed.score' | transloco: { correct: run.rightFirstTime(), total: run.answered() } }}</p>
              }
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
                <app-block-list [blocks]="screen().blocks" />
              }
            }
          }
        </article>

        <nav class="screen-nav">
          @if (index() > 0) {
            <button type="button" (click)="goTo(index() - 1)">← {{ 'session.previousScreen' | transloco }}</button>
          }
          @if (screen().kind === 'start' && timed() && !run.started()) {
            <button type="button" class="primary" (click)="run.start(); goTo(1)">{{ 'timed.start' | transloco }} →</button>
          } @else if (index() < plan().screens.length - 1) {
            <button type="button" class="primary" (click)="goTo(index() + 1)">
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

  protected readonly finishedIn = signal<number | null>(null);
  protected readonly outlineOpen = signal(false);
  protected readonly opened = computed(() => {
    const session = this.learn.session();
    return session && session.subject === this.subject() && session.id === this.session() ? session : null;
  });
  protected readonly plan = computed(() => sessionPlan(this.opened()?.parts ?? []));
  protected readonly index = computed(() => {
    const wanted = Number(this.screenParam() ?? 1) - 1;
    const last = this.plan().screens.length - 1;
    return Number.isInteger(wanted) ? Math.min(Math.max(wanted, 0), last) : 0;
  });
  protected readonly screen = computed<SessionScreen>(() => this.plan().screens[this.index()]);
  protected readonly where = computed(() => {
    const screen = this.screen();
    const part = this.plan().parts[screen.part]?.part;
    return part?.kind === 'notion' ? (part.title ?? '') : (this.opened()?.title ?? '');
  });
  protected readonly timed = computed(() => this.opened()?.kind !== 'lesson');
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
              this.run.begin(opened.kind, opened.duration);
              this.resumeStoredScreen(opened.kind === 'lesson');
            }
          },
          () => undefined,
        );
      });
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
    void this.router.navigate([], { queryParams: { screen: index + 1 }, replaceUrl: true });
    scrollTo({ top: 0 });
  }

  /** → and ← change screen, unless the learner is typing. */
  protected onKey(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    if (event.altKey || event.ctrlKey || event.metaKey || target.closest('input, textarea, select, [contenteditable]')) {
      return;
    }
    if (event.key === 'ArrowRight' && this.index() < this.plan().screens.length - 1 && !(this.timed() && !this.run.started())) {
      this.goTo(this.index() + 1);
    } else if (event.key === 'ArrowLeft' && this.index() > 0) {
      this.goTo(this.index() - 1);
    }
  }

  protected async finish(): Promise<void> {
    const seconds = this.run.spentSeconds();
    this.run.end();
    await this.learn.finishSession(seconds);
    this.finishedIn.set(Math.max(1, Math.round(seconds / 60)));
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

function positionKey(subject: string, session: string): string {
  return `screen.${subject}.${session}`;
}

function screenNumbers(count: number): string[] {
  return Array.from({ length: count }, (_, index) => String(index + 1));
}

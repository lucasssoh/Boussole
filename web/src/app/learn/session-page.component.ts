import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { BlockListComponent } from './blocks/block-list.component';
import { clock } from './blocks/video-block.component';
import { CourseOutlineComponent } from './course-outline.component';
import { LearnFacade } from './learn.facade';
import { SessionPage, sessionPages } from './session-pages';
import { SessionRun } from './session-run';
import { SidePanelComponent } from './side-panel.component';

/**
 * The session player: the course outline, the session one page at a time, and the video or
 * document beside it. A page shows its steps one after the other; the page number is in the
 * address, so a reload comes back to it. A lesson ends when the learner says so; a review or a
 * mock exam is timed.
 */
@Component({
  selector: 'app-session-page',
  imports: [RichTextComponent, RouterLink, TranslocoPipe, BlockListComponent, CourseOutlineComponent, SidePanelComponent],
  providers: [SessionRun],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'player',
    '[class.with-panel]': 'learn.panel() !== null',
    '(document:keydown)': 'onKey($event)',
  },
  template: `
    <nav class="outline" [attr.aria-label]="'session.contents' | transloco">
      @if (learn.course(); as course) {
        <a class="outline-course" [routerLink]="['/subjects', course.id]">{{ course.name }}</a>
        <app-course-outline [course]="course" [current]="session()" [attr.lang]="course.lang" />
      }
    </nav>

    <main class="session">
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      @if (opened(); as current) {
        <article [attr.lang]="learn.course()?.lang">
          <header class="session-header" [class.compact]="pageIndex() > 0">
            <p class="eyebrow">
              <a [routerLink]="['/subjects', current.subject]">{{ learn.course()?.name }}</a> ·
              {{ current.chapterTitle }} ·
              <span class="tag" [class]="current.kind">{{ 'kind.' + current.kind | transloco }}</span>
              {{ (timed() ? 'course.timed' : 'course.estimate') | transloco: { minutes: current.duration } }}
            </p>
            <h1><app-rich-text [text]="current.title" /></h1>
            @if (pageIndex() === 0) {
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
            }
          </header>

          @if (timed()) {
            <div class="timer" [class.over]="run.timeIsUp()" role="timer">
              @if (!run.started()) {
                <p>{{ 'timed.intro' | transloco: { minutes: current.duration, questions: roundedQuestionMinutes() } }}</p>
                <button type="button" class="primary" (click)="run.start()">{{ 'timed.start' | transloco }}</button>
              } @else if (run.timeIsUp()) {
                <p>{{ 'timed.over' | transloco }}</p>
              } @else {
                <p>{{ 'timed.remaining' | transloco: { time: remaining() } }}</p>
              }
            </div>
          }

          @if (run.started() && page(); as shown) {
            <ol class="stepper" [attr.aria-label]="'session.contents' | transloco">
              @for (item of pages(); track $index) {
                <li [class.current]="$index === pageIndex()" [class.seen]="$index < pageIndex()">
                  <button type="button" class="quiet" [attr.aria-current]="$index === pageIndex() ? 'step' : null" (click)="goTo($index)">
                    <span class="step-number">{{ $index + 1 }}</span>
                    <app-rich-text class="step-title" [text]="pageTitle(item)" />
                  </button>
                </li>
              }
            </ol>

            <section class="part" [class]="shown.part.kind" [attr.id]="shown.part.id">
              @if (shown.part.kind !== 'text') {
                <h2 [class.part-title]="shown.part.kind !== 'notion'"><app-rich-text [text]="pageTitle(shown)" /></h2>
              }
              @for (step of shown.steps.slice(0, revealed()); track $index; let last = $last) {
                <div class="step" [class.new]="last && revealed() > 1">
                  <app-block-list [blocks]="step" />
                </div>
              }
            </section>

            <nav class="pager">
              @if (pageIndex() > 0) {
                <button type="button" (click)="previous()">← {{ 'session.previousPage' | transloco }}</button>
              }
              @if (revealed() < shown.steps.length) {
                <button type="button" class="primary" (click)="next()">{{ 'session.continue' | transloco }} ↓</button>
              } @else if (pageIndex() < pages().length - 1) {
                <button type="button" class="primary" (click)="next()">
                  {{ 'session.nextPage' | transloco }} : <app-rich-text [text]="pageTitle(pages()[pageIndex() + 1])" /> →
                </button>
              }
              <span class="pager-count">{{ pageIndex() + 1 }} / {{ pages().length }}</span>
            </nav>

            @if (atEnd()) {
              <footer class="session-end">
                @if (timed() && run.answered() > 0) {
                  <p class="score">{{ 'timed.score' | transloco: { correct: run.rightFirstTime(), total: run.answered() } }}</p>
                }
                @if (finishedIn() === null) {
                  <button type="button" class="primary" (click)="finish()">{{ 'session.finish' | transloco }}</button>
                } @else {
                  <p class="verdict correct">{{ 'session.finished' | transloco: { minutes: finishedIn() } }}</p>
                }
                <nav class="session-nav">
                  @if (current.previous) {
                    <a [routerLink]="['/subjects', current.subject, 'sessions', current.previous]">← {{ 'session.previous' | transloco }}</a>
                  }
                  @if (current.next) {
                    <a class="next" [routerLink]="['/subjects', current.subject, 'sessions', current.next]">{{ 'session.next' | transloco }} →</a>
                  }
                </nav>
              </footer>
            }
          }
        </article>
      }
    </main>

    @if (learn.panel()) {
      <app-side-panel />
    }
  `,
})
export class SessionPageComponent {
  readonly subject = input.required<string>();
  readonly session = input.required<string>();
  /** The page shown, from 1, kept in the address. */
  readonly pageParam = input<string | undefined>(undefined, { alias: 'page' });

  protected readonly learn = inject(LearnFacade);
  protected readonly run = inject(SessionRun);
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);

  protected readonly finishedIn = signal<number | null>(null);
  protected readonly revealed = signal(1);
  protected readonly opened = computed(() => {
    const session = this.learn.session();
    return session && session.subject === this.subject() && session.id === this.session() ? session : null;
  });
  protected readonly pages = computed(() => sessionPages(this.opened()?.parts ?? []));
  protected readonly pageIndex = computed(() => {
    const wanted = Number(this.pageParam() ?? 1) - 1;
    return Number.isInteger(wanted) ? Math.min(Math.max(wanted, 0), Math.max(this.pages().length - 1, 0)) : 0;
  });
  protected readonly page = computed<SessionPage | undefined>(() => this.pages()[this.pageIndex()]);
  protected readonly atEnd = computed(
    () => this.pageIndex() === this.pages().length - 1 && this.revealed() >= (this.page()?.steps.length ?? 0),
  );
  protected readonly timed = computed(() => this.opened()?.kind !== 'lesson');
  protected readonly roundedQuestionMinutes = computed(() => Math.round(this.opened()?.questionMinutes ?? 0));
  protected readonly remaining = computed(() => clock(this.run.remainingSeconds() ?? 0));

  constructor() {
    effect(() => {
      const subject = this.subject();
      const session = this.session();
      untracked(() => {
        this.finishedIn.set(null);
        this.revealed.set(1);
        void this.learn.openSession(subject, session).then(
          () => {
            const opened = this.learn.session();
            this.run.begin(opened && opened.kind !== 'lesson' ? opened.duration : null);
          },
          () => undefined,
        );
      });
    });
  }

  protected pageTitle(page: SessionPage): string {
    return page.part.kind === 'notion' ? (page.part.title ?? '') : this.transloco.translate(`part.${page.part.kind}`);
  }

  protected next(): void {
    const steps = this.page()?.steps.length ?? 0;
    if (this.revealed() < steps) {
      this.revealed.update((count) => count + 1);
      setTimeout(() => document.querySelector('.step.new')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
    } else if (this.pageIndex() < this.pages().length - 1) {
      this.goTo(this.pageIndex() + 1);
    }
  }

  /** The previous page shows whole: it has been read already. */
  protected previous(): void {
    if (this.pageIndex() > 0) {
      this.goTo(this.pageIndex() - 1, true);
    }
  }

  protected goTo(index: number, whole = false): void {
    this.revealed.set(whole ? (this.pages()[index]?.steps.length ?? 1) : 1);
    void this.router.navigate([], { queryParams: { page: index + 1 }, replaceUrl: true });
    scrollTo({ top: 0 });
  }

  /** → and ← turn the pages, unless the learner is typing. */
  protected onKey(event: KeyboardEvent): void {
    const target = event.target as HTMLElement;
    if (event.altKey || event.ctrlKey || event.metaKey || target.closest('input, textarea, select, [contenteditable]')) {
      return;
    }
    if (event.key === 'ArrowRight') {
      this.next();
    } else if (event.key === 'ArrowLeft') {
      this.previous();
    }
  }

  protected async finish(): Promise<void> {
    const seconds = this.run.spentSeconds();
    await this.learn.finishSession(seconds);
    this.finishedIn.set(Math.max(1, Math.round(seconds / 60)));
  }
}

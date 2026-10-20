import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { BlockListComponent } from './blocks/block-list.component';
import { clock } from './blocks/video-block.component';
import { CourseOutlineComponent } from './course-outline.component';
import { LearnFacade } from './learn.facade';
import { SessionRun } from './session-run';
import { SidePanelComponent } from './side-panel.component';

/**
 * The session player: the course outline, the session, and the video or document beside it.
 * A lesson ends when the learner says so; a review or mock exam is timed.
 */
@Component({
  selector: 'app-session-page',
  imports: [RichTextComponent, RouterLink, TranslocoPipe, BlockListComponent, CourseOutlineComponent, SidePanelComponent],
  providers: [SessionRun],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'player', '[class.with-panel]': 'learn.panel() !== null' },
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
          <header class="session-header">
            <p class="eyebrow">
              <a [routerLink]="['/subjects', current.subject]">{{ learn.course()?.name }}</a> ·
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

          @if (run.started()) {
            @for (part of current.parts; track $index) {
              <section class="part" [class]="part.kind" [attr.id]="part.id">
                @if (part.kind === 'notion') {
                  <h2><app-rich-text [text]="part.title ?? ''" /></h2>
                } @else if (part.kind !== 'text') {
                  <h2 class="part-title">{{ 'part.' + part.kind | transloco }}</h2>
                }
                <app-block-list [blocks]="part.blocks" />
              </section>
            }

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

  protected readonly learn = inject(LearnFacade);
  protected readonly run = inject(SessionRun);

  protected readonly finishedIn = signal<number | null>(null);
  protected readonly opened = computed(() => {
    const session = this.learn.session();
    return session && session.subject === this.subject() && session.id === this.session() ? session : null;
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
            this.run.begin(opened && opened.kind !== 'lesson' ? opened.duration : null);
            scrollTo({ top: 0 });
          },
          () => undefined,
        );
      });
    });
  }

  protected async finish(): Promise<void> {
    const seconds = this.run.spentSeconds();
    await this.learn.finishSession(seconds);
    this.finishedIn.set(Math.max(1, Math.round(seconds / 60)));
  }
}

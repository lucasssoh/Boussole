import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { CourseOutlineComponent } from './course-outline.component';
import { LearnFacade } from './learn.facade';

/** A subject: its plan, and its lexicon. */
@Component({
  selector: 'app-course-page',
  imports: [RichTextComponent, RouterLink, CourseOutlineComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page narrow">
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      @if (learn.course(); as course) {
        @if (course.id === subject()) {
          <div [attr.lang]="course.lang">
            <h1>{{ course.name }}</h1>
            @if (course.description) {
              <p class="lead">{{ course.description }}</p>
            }
            @if (nextSession(); as next) {
              <p>
                <a class="button primary" [routerLink]="['/subjects', course.id, 'sessions', next.id]">
                  {{ 'course.continue' | transloco }} : <app-rich-text [text]="next.title" />
                </a>
              </p>
            }
            <h2>{{ 'course.sessions' | transloco }}</h2>
            <app-course-outline [course]="course" [detailed]="true" />
            @if (course.lexicon.length > 0) {
              <details class="lexicon">
                <summary>{{ 'course.lexicon' | transloco }} ({{ course.lexicon.length }})</summary>
                <dl>
                  @for (term of course.lexicon; track term.id) {
                    <dt>{{ term.term }}</dt>
                    <dd><app-rich-text [text]="term.definition" /></dd>
                  }
                </dl>
              </details>
            }
          </div>
        }
      }
    </main>
  `,
})
export class CoursePageComponent {
  readonly subject = input.required<string>();
  protected readonly learn = inject(LearnFacade);

  /** The first written session not finished yet, in the order of the syllabus. */
  protected readonly nextSession = computed(() =>
    this.learn.course()?.chapters.flatMap((chapter) => chapter.sessions).find((entry) => entry.written && !entry.finished));

  constructor() {
    effect(() => void this.learn.openCourse(this.subject()).catch(() => undefined));
  }
}

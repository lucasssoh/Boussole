import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { SessionGridComponent } from './session-grid.component';
import { LearnFacade } from './learn.facade';

/** A subject: its plan, and its lexicon. */
@Component({
  selector: 'app-course-page',
  imports: [IconComponent, RichTextComponent, RouterLink, SessionGridComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'subject-scope', '[style.--subject]': 'color()' },
  template: `
    <main class="page wide">
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
            @if (course.resume; as next) {
              <p>
                <a class="button primary" [routerLink]="['/subjects', course.id, 'sessions', next.id]">
                  {{ 'course.resume' | transloco }} : <app-rich-text [text]="next.title" /> <app-icon name="arrow-right" />
                </a>
              </p>
            } @else {
              <p class="note">{{ 'course.allDone' | transloco }}</p>
            }
            <h2>{{ 'course.sessions' | transloco }}</h2>
            <app-session-grid [course]="course" [resume]="course.resume?.id ?? null" />
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
  protected readonly color = computed(() => subjectColor(this.subject()));

  constructor() {
    effect(() => void this.learn.openCourse(this.subject()).catch(() => undefined));
  }
}

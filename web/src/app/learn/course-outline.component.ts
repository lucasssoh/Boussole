import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { Course } from '../core/api';

/** The chapters and sessions of a course; sessions not yet written show without a link. */
@Component({
  selector: 'app-course-outline',
  imports: [RichTextComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (chapter of course().chapters; track chapter.id) {
      <section class="outline-chapter">
        <h3><app-rich-text [text]="chapter.title" /></h3>
        <ol>
          @for (entry of chapter.sessions; track entry.id) {
            <li [class.current]="entry.id === current()" [class.finished]="entry.finished">
              @if (entry.written) {
                <a [routerLink]="['/subjects', course().id, 'sessions', entry.id]">
                  <span class="outline-mark" aria-hidden="true">{{ entry.finished ? '✓' : '○' }}</span>
                  <app-rich-text class="outline-title" [text]="entry.title" />
                </a>
              } @else {
                <span class="unwritten">
                  <span class="outline-mark" aria-hidden="true">·</span>
                  <app-rich-text class="outline-title" [text]="entry.title" />
                </span>
              }
              @if (detailed()) {
                <span class="outline-meta">
                  <span class="tag" [class]="entry.kind">{{ 'kind.' + entry.kind | transloco }}</span>
                  {{ (entry.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: entry.duration } }}
                  @if (!entry.written) {
                    · {{ 'course.notWritten' | transloco }}
                  }
                </span>
                <app-rich-text class="outline-goal" [text]="entry.goal" />
              }
            </li>
          }
        </ol>
      </section>
    }
  `,
})
export class CourseOutlineComponent {
  readonly course = input.required<Course>();
  readonly current = input<string | null>(null);
  /** With kind, duration and goal of each session, as on the course page. */
  readonly detailed = input(false);
}

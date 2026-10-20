import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { Chapter, Course } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';

/**
 * The sessions of a course as a grid of cards, chapter by chapter: finished, to resume, written,
 * or not written yet. A written session is a link; one not written yet is shown flat.
 */
@Component({
  selector: 'app-session-grid',
  imports: [RouterLink, TranslocoPipe, RichTextComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (chapter of shown(); track chapter.id) {
      <section class="grid-chapter">
        <div class="grid-chapter-head">
          <h3><app-rich-text [text]="chapter.title" /></h3>
          <span class="grid-count">{{ finishedIn(chapter) }} / {{ chapter.sessions.length }}</span>
        </div>
        <ul class="session-grid">
          @for (entry of chapter.sessions; track entry.id) {
            <li>
              @if (entry.written) {
                <a
                  [class]="'session-card ' + entry.kind"
                  [class.finished]="entry.finished"
                  [class.resume]="entry.id === resume()"
                  [routerLink]="['/subjects', course().id, 'sessions', entry.id]"
                >
                  <span class="session-card-top">
                    <span class="session-mark" aria-hidden="true">{{ entry.finished ? '✓' : entry.id === resume() ? '▶' : '' }}</span>
                    <span class="tag" [class]="entry.kind">{{ 'kind.' + entry.kind | transloco }}</span>
                    <span class="session-time">
                      {{ (entry.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: entry.duration } }}
                    </span>
                  </span>
                  <span class="session-card-title"><app-rich-text [text]="entry.title" /></span>
                  <span class="session-card-goal"><app-rich-text [text]="entry.goal" /></span>
                </a>
              } @else {
                <div class="session-card unwritten">
                  <span class="session-card-top">
                    <span class="tag">{{ 'course.notWritten' | transloco }}</span>
                  </span>
                  <span class="session-card-title"><app-rich-text [text]="entry.title" /></span>
                </div>
              }
            </li>
          }
        </ul>
      </section>
    }
  `,
})
export class SessionGridComponent {
  readonly course = input.required<Course>();
  /** The chapters to show; all of them when not given. */
  readonly chapters = input<Chapter[] | null>(null);
  readonly resume = input<string | null>(null);

  protected readonly shown = computed(() => this.chapters() ?? this.course().chapters);

  protected finishedIn(chapter: Chapter): number {
    return chapter.sessions.filter((entry) => entry.finished).length;
  }
}

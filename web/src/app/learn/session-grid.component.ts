import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { Chapter, Course, SessionEntry } from '../core/api';
import { ServerSettingsService } from '../core/server-settings.service';
import { RichTextComponent } from '../shared/rich-text.component';

/**
 * The sessions of a course as a grid of cards, chapter by chapter: finished, to resume, written,
 * or not written yet. A written session is a link; one not written yet is shown flat. A session
 * waiting for its prerequisites says which on hover: locked in guided mode, advised after them in free mode.
 */
@Component({
  selector: 'app-session-grid',
  imports: [IconComponent, RouterLink, TranslocoPipe, RichTextComponent],
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
                  [class.locked]="locked(entry)"
                  [attr.title]="waiting(entry) ? (afterKey() | transloco: { titles: titles(entry) }) : null"
                  [routerLink]="['/subjects', course().id, 'sessions', entry.id]"
                >
                  <span class="session-card-top">
                    <span class="session-mark">
                      @if (entry.finished) {
                        <app-icon name="check" />
                      } @else if (locked(entry)) {
                        <app-icon name="lock" />
                      } @else if (entry.id === resume()) {
                        <app-icon name="play" />
                      }
                    </span>
                    <span class="tag" [class]="entry.kind">{{ 'kind.' + entry.kind | transloco }}</span>
                    <span class="session-time">
                      {{ (entry.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: entry.duration } }}
                    </span>
                  </span>
                  <span class="session-card-title"><app-rich-text [text]="entry.title" /></span>
                  <span class="session-card-goal"><app-rich-text [text]="entry.goal" /></span>
                  @if (waiting(entry)) {
                    <span class="visually-hidden">{{ afterKey() | transloco: { titles: titles(entry) } }}</span>
                  }
                  @if (entry.questions > 0) {
                    <span class="session-progress" [attr.aria-label]="'course.progressLabel' | transloco: entry">
                      <span class="session-progress-track">
                        <span class="session-progress-right" [style.width.%]="(100 * entry.right) / entry.questions"></span>
                        <span class="session-progress-missed" [style.width.%]="(100 * (entry.answered - entry.right)) / entry.questions"></span>
                      </span>
                      <span class="session-progress-text">
                        @if (entry.answered === 0) {
                          {{ 'course.questions' | transloco: entry }}
                        } @else {
                          {{ 'course.progress' | transloco: entry }}
                        }
                      </span>
                    </span>
                  }
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

  protected readonly settings = inject(ServerSettingsService);

  protected readonly shown = computed(() => this.chapters() ?? this.course().chapters);

  /** The sessions to finish first, said on hover and to screen readers rather than on the card. */
  protected waiting(entry: SessionEntry): boolean {
    return !entry.finished && entry.waitingFor.length > 0;
  }

  protected afterKey(): string {
    return this.settings.freeNavigation() ? 'course.advisedAfter' : 'course.after';
  }

  protected locked(entry: SessionEntry): boolean {
    return this.waiting(entry) && !this.settings.freeNavigation();
  }

  protected titles(entry: SessionEntry): string {
    return entry.waitingFor.map((prerequisite) => prerequisite.title).join(', ');
  }

  protected finishedIn(chapter: Chapter): number {
    return chapter.sessions.filter((entry) => entry.finished).length;
  }
}

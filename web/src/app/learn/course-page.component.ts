import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { SessionGridComponent } from './session-grid.component';
import { LearnFacade } from './learn.facade';
import { TrackLanguagesComponent } from './track-languages.component';

/** A subject: its plan, and its lexicon. */
@Component({
  selector: 'app-course-page',
  imports: [IconComponent, RichTextComponent, RouterLink, SessionGridComponent, TranslocoPipe, TrackLanguagesComponent],
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
            <app-track-languages [languages]="course.languages" [current]="course.lang" (choose)="followIn(course.id, $event)" />
            @if (course.description) {
              <p class="lead">{{ course.description }}</p>
            }
            @if (course.authors.length > 0 || course.contributors.length > 0 || course.writtenWith.length > 0) {
              <dl class="credits">
                @if (course.authors.length > 0) {
                  <dt>{{ 'credits.by' | transloco }}</dt>
                  <dd>
                    @for (author of course.authors; track author.name; let last = $last) {
                      @if (author.url) { <a [href]="author.url" target="_blank" rel="noopener">{{ author.name }}</a> } @else { {{ author.name }} }{{ last ? '' : ', ' }}
                    }
                  </dd>
                }
                @if (course.contributors.length > 0) {
                  <dt>{{ 'credits.with' | transloco }}</dt>
                  <dd>
                    @for (contributor of course.contributors; track contributor.name; let last = $last) {
                      {{ contributor.name }}{{ contributor.role ? ' (' + contributor.role + ')' : '' }}{{ last ? '' : ', ' }}
                    }
                  </dd>
                }
                @if (course.writtenWith.length > 0) {
                  <dt>{{ 'credits.writtenWith' | transloco }}</dt>
                  <dd>
                    @for (writer of course.writtenWith; track writer.who; let last = $last) {
                      <span [class.model-name]="writer.model">{{ writer.who }}</span> ({{ (writer.sessions === 1 ? 'credits.session' : 'credits.sessions') | transloco: { count: writer.sessions } }}){{ last ? '' : ', ' }}
                    }
                  </dd>
                }
              </dl>
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

  protected followIn(subject: string, language: string): void {
    this.learn.chooseTrackLanguage(subject, language);
    void this.learn.openCourse(subject).catch(() => undefined);
  }

  constructor() {
    effect(() => void this.learn.openCourse(this.subject()).catch(() => undefined));
  }
}

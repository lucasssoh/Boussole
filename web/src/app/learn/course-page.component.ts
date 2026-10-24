import { ChangeDetectionStrategy, Component, computed, effect, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { formatPoints } from '../shared/ranks';
import { LanguageService } from '../core/language.service';
import { TrackScore } from '../core/api';
import { CourseMapComponent } from './course-map.component';
import { LearnFacade } from './learn.facade';
import { TrackLanguagesComponent } from './track-languages.component';

/** A subject: its plan, and its lexicon. */
@Component({
  selector: 'app-course-page',
  imports: [CourseMapComponent, IconComponent, RichTextComponent, RouterLink, TranslocoPipe, TrackLanguagesComponent],
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
            @if (course.level || course.domain) {
              <p class="track-tags">
                @if (course.level) {
                  <span class="track-tag level">{{ 'course.level.' + course.level | transloco }}</span>
                }
                @if (course.domain) {
                  <span class="track-tag">{{ 'domain.' + course.domain | transloco }}</span>
                }
              </p>
            }
            @if (course.score.possible > 0) {
              <section class="track-score">
                <span class="panel-label">{{ 'course.score' | transloco }}</span>
                <strong class="panel-value">{{ points(course.score.earned) }} <span>/ {{ points(course.score.possible) }}</span></strong>
                <span class="score-bar">
                  <span class="score-earned" [style.width.%]="(100 * course.score.earned) / course.score.possible"></span>
                  <span class="score-missed" [style.width.%]="(100 * course.score.missed) / course.score.possible"></span>
                </span>
                <span class="panel-note">{{ 'course.scoreDetail' | transloco: { earned: points(course.score.earned), missed: points(course.score.missed), toPlay: points(toPlay(course.score)) } }}</span>
              </section>
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
            <app-course-map [course]="course" [resume]="course.resume?.id ?? null" />
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
  private readonly language = inject(LanguageService);

  protected points(points: number): string {
    return formatPoints(points, this.language.current());
  }

  protected toPlay(score: TrackScore): number {
    return Math.max(0, score.possible - score.earned - score.missed);
  }

  protected followIn(subject: string, language: string): void {
    this.learn.chooseTrackLanguage(subject, language);
    void this.learn.openCourse(subject).catch(() => undefined);
  }

  constructor() {
    effect(() => void this.learn.openCourse(this.subject()).catch(() => undefined));
  }
}

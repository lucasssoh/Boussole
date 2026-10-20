import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { CourseSummary } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { LearnFacade } from './learn.facade';

/**
 * The home page: where to resume, on top (the session in progress in the subject studied last,
 * or the first session of a subject not started yet), then every subject with its progress.
 */
@Component({
  selector: 'app-subject-list',
  imports: [RouterLink, TranslocoPipe, RichTextComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page narrow">
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      @if (resume(); as subject) {
        @if (subject.resume; as next) {
          <section class="resume-card" [attr.lang]="subject.lang">
            <p class="eyebrow">{{ subject.name }} · <app-rich-text [text]="next.chapterTitle" /></p>
            <h1><app-rich-text [text]="next.title" /></h1>
            <p class="resume-meta">
              <span class="tag" [class]="next.kind">{{ 'kind.' + next.kind | transloco }}</span>
              {{ (next.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: next.duration } }}
            </p>
            <a class="button primary" [routerLink]="['/subjects', subject.id, 'sessions', next.id]">
              {{ (subject.lastStudied ? 'course.resume' : 'course.start') | transloco }} →
            </a>
          </section>
        }
      }
      @if (learn.subjects(); as subjects) {
        <h2>{{ 'home.title' | transloco }}</h2>
        @if (subjects.length === 0) {
          <p class="frame note">{{ 'home.empty' | transloco }}</p>
        }
        <ul class="cards">
          @for (subject of subjects; track subject.id) {
            <li>
              <a class="card" [routerLink]="['/subjects', subject.id]" [attr.lang]="subject.lang">
                <span class="card-title">{{ subject.name }}</span>
                <progress [max]="subject.planned || 1" [value]="subject.finished"></progress>
                <span class="card-meta">{{ 'home.progress' | transloco: subject }}</span>
                @if (subject.resume; as next) {
                  <span class="card-text">{{ 'course.next' | transloco: { title: '' } }}<app-rich-text [text]="next.title" /></span>
                } @else {
                  <span class="card-text">{{ 'course.allDone' | transloco }}</span>
                }
              </a>
            </li>
          }
        </ul>
      }
    </main>
  `,
})
export class SubjectListComponent {
  protected readonly learn = inject(LearnFacade);

  /** The subject studied last that has a session to resume, else the first one that has. */
  protected readonly resume = computed<CourseSummary | undefined>(() => {
    const subjects = (this.learn.subjects() ?? []).filter((subject) => subject.resume !== null);
    return [...subjects].sort((a, b) => (b.lastStudied ?? '').localeCompare(a.lastStudied ?? ''))[0];
  });

  constructor() {
    void this.learn.loadSubjects().catch(() => undefined);
  }
}

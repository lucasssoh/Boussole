import { ChangeDetectionStrategy, Component, computed, effect, inject, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { CourseSummary } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { LearnFacade } from './learn.facade';
import { SessionGridComponent } from './session-grid.component';

/**
 * The home page: where to resume, on top (the session in progress in the subject studied last,
 * or the first session of a subject not started yet), then every subject with its progress, and
 * apart, the guide to Boussole, which is never what to resume once the learner has a subject.
 */
@Component({
  selector: 'app-subject-list',
  imports: [IconComponent, RouterLink, TranslocoPipe, RichTextComponent, SessionGridComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide">
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      @if (resume(); as subject) {
        @if (subject.resume; as next) {
          <section class="resume-card subject-scope" [attr.lang]="subject.lang" [style.--subject]="color(subject.id)">
            <p class="eyebrow">{{ subject.name }} · <app-rich-text [text]="next.chapterTitle" /></p>
            <h1><app-rich-text [text]="next.title" /></h1>
            <p class="resume-meta">
              <span class="tag" [class]="next.kind">{{ 'kind.' + next.kind | transloco }}</span>
              {{ (next.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: next.duration } }}
            </p>
            <a class="button primary" [routerLink]="['/subjects', subject.id, 'sessions', next.id]">
              {{ (subject.lastStudied ? 'course.resume' : 'course.start') | transloco }} <app-icon name="arrow-right" />
            </a>
          </section>
        }
      }
      @if (resumeChapter(); as chapter) {
        @if (learn.course(); as course) {
          <div class="subject-scope" [style.--subject]="color(course.id)" [attr.lang]="course.lang">
            <app-session-grid [course]="course" [chapters]="[chapter]" [resume]="course.resume?.id ?? null" />
          </div>
        }
      }
      @if (ownSubjects(); as subjects) {
        <h2>{{ 'home.title' | transloco }}</h2>
        @if (subjects.length === 0) {
          <p class="frame note">{{ 'home.empty' | transloco }}</p>
        }
        <ul class="cards">
          @for (subject of subjects; track subject.id) {
            <li>
              <a class="card subject-scope" [routerLink]="['/subjects', subject.id]" [attr.lang]="subject.lang" [style.--subject]="color(subject.id)">
                <span class="card-title">{{ subject.name }}</span>
                <span class="xp-track"><span class="xp-fill" [style.width.%]="(100 * subject.finished) / (subject.planned || 1)"></span></span>
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
      @if (guides().length > 0) {
        <h2>{{ 'home.guideTitle' | transloco }}</h2>
        <ul class="cards">
          @for (guide of guides(); track guide.id) {
            <li>
              <a class="card guide-card subject-scope" [routerLink]="['/subjects', guide.id]" [attr.lang]="guide.lang">
                <span class="card-title"><img src="favicon.svg" alt="" class="guide-logo" /> {{ guide.name }}</span>
                <span class="card-text">{{ 'home.guideText' | transloco }}</span>
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

  /** The learner's own subjects, the guide left out; undefined until they are loaded. */
  protected readonly ownSubjects = computed(() => this.learn.subjects()?.filter((subject) => !subject.guide));

  protected readonly guides = computed(() => (this.learn.subjects() ?? []).filter((subject) => subject.guide));

  /**
   * The subject studied last that has a session to resume, else the first one that has; the
   * guide only while the learner has no subject of their own.
   */
  protected readonly resume = computed<CourseSummary | undefined>(() => {
    const own = this.ownSubjects() ?? [];
    const subjects = (own.length > 0 ? own : this.guides()).filter((subject) => subject.resume !== null);
    return [...subjects].sort((a, b) => (b.lastStudied ?? '').localeCompare(a.lastStudied ?? ''))[0];
  });

  protected readonly color = subjectColor;

  /** The chapter of the session to resume, whose sessions show under it. */
  protected readonly resumeChapter = computed(() => {
    const course = this.learn.course();
    const subject = this.resume();
    if (!course || !subject || course.id !== subject.id || !course.resume) {
      return undefined;
    }
    return course.chapters.find((chapter) => chapter.sessions.some((entry) => entry.id === course.resume!.id));
  });

  constructor() {
    void this.learn.loadSubjects().catch(() => undefined);
    effect(() => {
      const subject = this.resume();
      if (subject) {
        untracked(() => void this.learn.openCourse(subject.id).catch(() => undefined));
      }
    });
  }
}

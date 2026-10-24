import { ChangeDetectionStrategy, Component, computed, effect, inject, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { CourseSummary } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { subjectColor } from '../shared/subject-color';
import { LearnFacade } from './learn.facade';
import { LoadingComponent } from '../shared/loading.component';
import { SessionGridComponent } from './session-grid.component';

/** Built-in tracks whose ID starts so are about Boussole itself. */
const HOW_TO = 'boussole-';

/**
 * The home page: where to resume, on top (the session in progress in the subject studied last,
 * or the first session of a subject not started yet), every subject of the learner with its
 * progress, then the tracks built into Boussole on two shelves: how to use Boussole, and others to
 * discover. The guide is what to resume only while there is no other subject.
 */
@Component({
  selector: 'app-subject-list',
  imports: [IconComponent, LoadingComponent, RouterLink, TranslocoPipe, RichTextComponent, SessionGridComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide">
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      <div class="home-top">
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
      </div>
      @if (resumeChapter(); as chapter) {
        @if (learn.course(); as course) {
          <div class="subject-scope" [style.--subject]="color(course.id)" [attr.lang]="course.lang">
            <app-session-grid [course]="course" [chapters]="[chapter]" [resume]="course.resume?.id ?? null" />
          </div>
        }
      }
      @if (learn.subjects() === null && !learn.unreachable()) {
        <app-loading />
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
      @for (shelf of shelves(); track shelf.kind) {
        @if (shelf.tracks.length > 0) {
          <section class="shelf" [class]="'shelf ' + shelf.kind">
            <h2>{{ 'home.' + shelf.kind | transloco }}</h2>
            <ul class="cards">
              @for (track of shelf.tracks; track track.id) {
                <li>
                  <a class="card shelf-card" [class.starter]="track.starter" [routerLink]="['/subjects', track.id]" [attr.lang]="track.lang">
                    <span class="card-title">{{ track.name }}</span>
                    <span class="card-meta">{{ (track.written === 1 ? 'home.session' : 'home.sessions') | transloco: { count: track.written } }}</span>
                  </a>
                </li>
              }
            </ul>
          </section>
        }
      }
    </main>
  `,
})
export class SubjectListComponent {
  protected readonly learn = inject(LearnFacade);

  /** The learner's own subjects, the built-in tracks left out; undefined until they are loaded. */
  protected readonly ownSubjects = computed(() => this.learn.subjects()?.filter((subject) => !subject.builtIn));

  protected readonly guides = computed(() => (this.learn.subjects() ?? []).filter((subject) => subject.builtIn && subject.starter));

  /**
   * The tracks built into Boussole, on two shelves: how to use Boussole (their IDs start with
   * boussole-, the guide first), and the others, to discover.
   */
  protected readonly shelves = computed(() => {
    const builtIn = (this.learn.subjects() ?? []).filter((subject) => subject.builtIn);
    return [
      { kind: 'howTo', tracks: builtIn.filter((track) => track.id.startsWith(HOW_TO)) },
      { kind: 'discover', tracks: builtIn.filter((track) => !track.id.startsWith(HOW_TO)) },
    ];
  });

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

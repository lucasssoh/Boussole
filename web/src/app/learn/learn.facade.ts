import { Injectable, computed, inject, signal } from '@angular/core';
import { BoussoleApi } from '../core/boussole-api';
import { Course, CourseSummary, LexiconTerm, Session } from '../core/api';

/** What opens beside the text: a YouTube video, a video file of the sources, or a document. */
export type SidePanel =
  | { kind: 'video'; label: string; youtubeId: string; start: number | null; end: number | null }
  | { kind: 'localVideo'; label: string; path: string; start: number | null; end: number | null }
  | { kind: 'pdf'; label: string; path: string; page: number | null };

/** The state of studying: the subjects, the course and the session open, and the side panel. */
@Injectable({ providedIn: 'root' })
export class LearnFacade {
  private readonly api = inject(BoussoleApi);

  readonly subjects = signal<CourseSummary[] | null>(null);
  readonly course = signal<Course | null>(null);
  readonly session = signal<Session | null>(null);
  readonly panel = signal<SidePanel | null>(null);
  readonly unreachable = signal(false);

  readonly lexicon = computed(() => this.course()?.lexicon ?? []);

  async loadSubjects(): Promise<void> {
    this.subjects.set(await this.reach(this.api.subjects()));
  }

  async openCourse(subject: string): Promise<void> {
    this.course.set(await this.reach(this.api.course(subject)));
  }

  async openSession(subject: string, session: string): Promise<void> {
    this.session.set(null);
    this.panel.set(null);
    const [course, opened] = await Promise.all([this.reach(this.api.course(subject)), this.reach(this.api.session(subject, session))]);
    this.course.set(course);
    this.session.set(opened);
  }

  /** The finished mark of a session shows in the course at once. */
  /** Returns the experience earned by finishing. */
  async finishSession(spentSeconds: number): Promise<number> {
    const session = this.session();
    if (!session) {
      return 0;
    }
    const finished = await this.api.finishSession(session.subject, session.id, spentSeconds);
    await this.openCourse(session.subject);
    return finished.xpGained;
  }

  /** The term a :term directive names: by its ID, else by its text, ignoring case. */
  term(id: string, text: string): LexiconTerm | undefined {
    const wanted = normalise(text);
    return this.lexicon().find((term) => id
      ? term.id === id
      : normalise(term.term) === wanted || term.aliases.some((alias) => normalise(alias) === wanted));
  }

  fileUrl(path: string): string {
    return this.api.fileUrl(this.course()?.id ?? this.session()?.subject ?? '', path);
  }

  /** The folder of the open session file, for the relative links and images of its Markdown. */
  sessionBase(): string {
    const session = this.session();
    if (!session) {
      return '';
    }
    const folder = session.file.substring(0, session.file.lastIndexOf('/') + 1);
    return this.api.fileUrl(session.subject, folder);
  }

  /** A :pdf directive names a file of the sources folder. */
  sourcePath(file: string): string {
    const folder = this.course()?.sourcesDir ?? '.';
    return folder === '.' || folder === '' ? file : `${folder.replace(/\/$/, '')}/${file}`;
  }

  private async reach<T>(call: Promise<T>): Promise<T> {
    try {
      const result = await call;
      this.unreachable.set(false);
      return result;
    } catch (error) {
      this.unreachable.set(true);
      throw error;
    }
  }
}

function normalise(text: string): string {
  return text.trim().replace(/\s+/g, ' ').toLowerCase();
}

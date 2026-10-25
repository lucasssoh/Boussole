import { Injectable, computed, effect, inject, signal, untracked } from '@angular/core';
import { BoussoleApi } from '../core/boussole-api';
import { Course, CourseSummary, LexiconTerm, Session } from '../core/api';
import { LanguageService } from '../core/language.service';
import { storeValue, storedText } from '../core/local-preferences';

/**
 * What opens beside the text: a YouTube video, a video file of the sources, a document or a web
 * page, or the list of everything the session cites.
 */
export type SidePanel =
  | { kind: 'documents' }
  | { kind: 'web'; label: string; url: string }
  | { kind: 'video'; label: string; youtubeId: string; start: number | null; end: number | null }
  | { kind: 'localVideo'; label: string; path: string; start: number | null; end: number | null }
  | { kind: 'pdf'; label: string; path: string; page: number | null };

/** The state of studying: the subjects, the course and the session open, and the side panel. */
@Injectable({ providedIn: 'root' })
export class LearnFacade {
  private readonly api = inject(BoussoleApi);
  private readonly interfaceLanguage = inject(LanguageService);

  readonly subjects = signal<CourseSummary[] | null>(null);
  readonly course = signal<Course | null>(null);
  readonly session = signal<Session | null>(null);
  readonly panel = signal<SidePanel | null>(null);
  /** The last document, page or video shown beside the text in this session, marked in the list. */
  readonly lastShown = signal<SidePanel | null>(null);
  readonly unreachable = signal(false);

  readonly lexicon = computed(() => this.course()?.lexicon ?? []);

  constructor() {
    effect(() => {
      const panel = this.panel();
      if (panel && panel.kind !== 'documents') {
        untracked(() => this.lastShown.set(panel));
      }
    });
  }

  async loadSubjects(): Promise<void> {
    this.subjects.set(await this.reach(this.api.subjects(this.interfaceLanguage.current())));
  }

  async openCourse(subject: string): Promise<void> {
    this.course.set(await this.reach(this.api.course(subject, this.trackLanguage(subject))));
  }

  async openSession(subject: string, session: string): Promise<void> {
    this.session.set(null);
    this.panel.set(null);
    this.lastShown.set(null);
    const lang = this.trackLanguage(subject);
    const [course, opened] = await Promise.all([
      this.reach(this.api.course(subject, lang)),
      this.reach(this.api.session(subject, session, lang)),
    ]);
    this.course.set(course);
    this.session.set(opened);
  }

  /**
   * The language a track is followed in: the learner's choice for it in this browser, else the
   * language of the interface; the server falls back on the track's first language.
   */
  trackLanguage(subject: string): string {
    return storedText(`track-language.${subject}`) || this.interfaceLanguage.current();
  }

  /** Follows a track in another language from now on; progress is the same in every language. */
  chooseTrackLanguage(subject: string, language: string): void {
    storeValue(`track-language.${subject}`, language);
  }

  /** The finished mark of a session shows in the course at once. */
  /** Returns the bonus earned by finishing without a mistake, 0 otherwise. */
  async finishSession(spentSeconds: number): Promise<number> {
    const session = this.session();
    if (!session) {
      return 0;
    }
    const finished = await this.api.finishSession(session.subject, session.id, spentSeconds);
    await this.openCourse(session.subject);
    return finished.bonus;
  }

  /** The term a :term directive names: by its ID, else by its text, ignoring case. */
  term(id: string, text: string): LexiconTerm | undefined {
    const wanted = normalise(text);
    return this.lexicon().find((term) => id
      ? term.id === id
      : normalise(term.term) === wanted || term.aliases.some((alias) => normalise(alias) === wanted));
  }

  fileUrl(path: string): string {
    return this.api.fileUrl(this.course()?.id ?? this.session()?.subject ?? '', path, this.session()?.lang ?? this.course()?.lang ?? null);
  }

  /** The folder of the open session file, for the relative links and images of its Markdown. */
  sessionBase(): string {
    const session = this.session();
    if (!session) {
      return '';
    }
    const folder = session.file.substring(0, session.file.lastIndexOf('/') + 1);
    return this.api.fileUrl(session.subject, folder, session.lang);
  }

  /** A :pdf directive names a file of the sources folder. */
  sourcePath(file: string): string {
    const folder = this.course()?.sourcesDir ?? '.';
    if (folder.startsWith('..')) {
      // A sources folder outside the subject's (SourcePaths.SOURCES on the server).
      return `@sources/${file}`;
    }
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

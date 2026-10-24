import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AttemptRequest, AttemptResult, Course, CourseSummary, PagePreview, RevealedHint, RevealedSolution, ServerInfo, Session, StudyStats } from './api';

/** The calls to the local server. Answers only come with an attempt or a solution asked for. */
@Injectable({ providedIn: 'root' })
export class BoussoleApi {
  private readonly http = inject(HttpClient);

  server(): Promise<ServerInfo> {
    return this.get('/api/server');
  }

  /** {@code lang}: the language to show each track in, when it exists in it. */
  subjects(lang: string | null = null): Promise<CourseSummary[]> {
    return this.get(`/api/subjects${query({ lang })}`);
  }

  course(subject: string, lang: string | null = null): Promise<Course> {
    return this.get(`/api/subjects/${enc(subject)}${query({ lang })}`);
  }

  session(subject: string, session: string, lang: string | null = null): Promise<Session> {
    return this.get(`/api/subjects/${enc(subject)}/sessions/${enc(session)}${query({ lang })}`);
  }

  stats(): Promise<StudyStats> {
    return this.get('/api/stats');
  }

  finishSession(subject: string, session: string, spentSeconds: number): Promise<{ bonus: number }> {
    return this.post(`/api/subjects/${enc(subject)}/sessions/${enc(session)}/finish`, { spentSeconds });
  }

  /**
   * {@code session}: the session the learner answers in, so that the subject resumes there;
   * {@code lang}: the version of the track the question was read in, whose answer is checked.
   */
  attempt(subject: string, exercise: string, request: AttemptRequest, session: string | null, lang: string | null = null): Promise<AttemptResult> {
    return this.post(`${exercisePath(subject, exercise)}/attempts${query({ session, lang })}`, request);
  }

  nextHint(subject: string, exercise: string, lang: string | null = null): Promise<RevealedHint> {
    return this.post(`${exercisePath(subject, exercise)}/hints${query({ lang })}`, {});
  }

  solution(subject: string, exercise: string, lang: string | null = null): Promise<RevealedSolution> {
    return this.get(`${exercisePath(subject, exercise)}/solution${query({ lang })}`);
  }

  /** Whether a web page cited by a course can show beside the text. */
  previewPage(url: string): Promise<PagePreview> {
    return this.get(`/api/references/preview?url=${enc(url)}`);
  }

  /**
   * The URL of a file of the subject folder, from its path relative to that folder, in the version
   * {@code lang} of the track. The language goes in the path, so that relative links still resolve.
   */
  fileUrl(subject: string, path: string, lang: string | null = null): string {
    const version = lang ? `/lang/${enc(lang)}` : '';
    return `/api/subjects/${enc(subject)}${version}/files/${path.split('/').map(enc).join('/')}`;
  }

  private get<T>(url: string): Promise<T> {
    return firstValueFrom(this.http.get<T>(url));
  }

  private post<T>(url: string, body: unknown): Promise<T> {
    return firstValueFrom(this.http.post<T>(url, body));
  }
}

function enc(part: string): string {
  return encodeURIComponent(part);
}

function exercisePath(subject: string, exercise: string): string {
  return `/api/subjects/${enc(subject)}/exercises/${enc(exercise)}`;
}

/** A query string of the values given, empty when none is. */
function query(values: Record<string, string | null>): string {
  const given = Object.entries(values).filter((entry): entry is [string, string] => entry[1] !== null && entry[1] !== '');
  return given.length === 0 ? '' : '?' + given.map(([key, value]) => `${key}=${enc(value)}`).join('&');
}

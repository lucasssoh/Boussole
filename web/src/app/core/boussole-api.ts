import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  AttemptRequest,
  AttemptResult,
  Course,
  CourseSummary,
  RevealedHint,
  RevealedSolution,
  ServerInfo,
  Session,
} from './api';

/** The calls to the local server. Answers only come with an attempt or a solution asked for. */
@Injectable({ providedIn: 'root' })
export class BoussoleApi {
  private readonly http = inject(HttpClient);

  server(): Promise<ServerInfo> {
    return this.get('/api/server');
  }

  subjects(): Promise<CourseSummary[]> {
    return this.get('/api/subjects');
  }

  course(subject: string): Promise<Course> {
    return this.get(`/api/subjects/${enc(subject)}`);
  }

  session(subject: string, session: string): Promise<Session> {
    return this.get(`/api/subjects/${enc(subject)}/sessions/${enc(session)}`);
  }

  finishSession(subject: string, session: string, spentSeconds: number): Promise<void> {
    return this.post(`/api/subjects/${enc(subject)}/sessions/${enc(session)}/finish`, { spentSeconds });
  }

  attempt(subject: string, exercise: string, request: AttemptRequest): Promise<AttemptResult> {
    return this.post(`${exercisePath(subject, exercise)}/attempts`, request);
  }

  nextHint(subject: string, exercise: string): Promise<RevealedHint> {
    return this.post(`${exercisePath(subject, exercise)}/hints`, {});
  }

  solution(subject: string, exercise: string): Promise<RevealedSolution> {
    return this.get(`${exercisePath(subject, exercise)}/solution`);
  }

  /** The URL of a file of the subject folder, from its path relative to that folder. */
  fileUrl(subject: string, path: string): string {
    return `/api/subjects/${enc(subject)}/files/${path.split('/').map(enc).join('/')}`;
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

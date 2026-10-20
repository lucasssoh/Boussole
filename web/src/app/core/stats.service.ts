import { Injectable, inject, signal } from '@angular/core';
import { StudyStats } from './api';
import { BoussoleApi } from './boussole-api';

/** The learner's experience, level and streak, shown in the header and refreshed after each answer. */
@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly api = inject(BoussoleApi);

  readonly stats = signal<StudyStats | null>(null);

  refresh(): void {
    this.api.stats().then((stats) => this.stats.set(stats), () => undefined);
  }
}

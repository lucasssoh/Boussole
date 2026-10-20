import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { ServerSettingsService } from './core/server-settings.service';
import { StatsService } from './core/stats.service';
import { SettingsDrawerComponent } from './core/settings-drawer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TranslocoPipe, SettingsDrawerComponent],
  template: `
    <header class="top-bar">
      <a class="brand" routerLink="/">
        <img class="brand-logo" src="favicon.svg" alt="" width="32" height="32" />
        Boussole
      </a>
      @if (stats.stats(); as current) {
        <div class="player-stats" [attr.aria-label]="'stats.label' | transloco: current">
          <span class="level-badge">{{ current.level }}</span>
          <span class="level-bar">
            <span class="level-line">{{ 'stats.level' | transloco: current }} · {{ current.levelXp }} / {{ current.levelSize }} XP</span>
            <span class="xp-track"><span class="xp-fill" [style.width.%]="(100 * current.levelXp) / current.levelSize"></span></span>
          </span>
          <span class="streak" [class.cold]="!current.studiedToday">
            <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2c1 4 5 5 5 10a5 5 0 01-10 0c0-2 1-3.5 2-4.5 0 2 1 3 2 3-1-3 0-6 1-8.5z"></path></svg>
            {{ current.streakDays }}
          </span>
        </div>
      }
      <nav class="top-actions">
        <app-settings-drawer />
      </nav>
    </header>
    <router-outlet />
  `,
})
export class App {
  protected readonly stats = inject(StatsService);

  constructor() {
    inject(ServerSettingsService).load();
    this.stats.refresh();
  }
}

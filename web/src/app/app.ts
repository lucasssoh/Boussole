import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LadderStep } from './core/api';
import { LanguageService } from './core/language.service';
import { RankPillComponent } from './shared/rank-pill.component';
import { PromotionComponent } from './shared/promotion.component';
import { formatPoints, roman } from './shared/ranks';
import { ServerSettingsService } from './core/server-settings.service';
import { StatsService } from './core/stats.service';
import { SettingsDrawerComponent } from './core/settings-drawer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslocoPipe, SettingsDrawerComponent, RankPillComponent, PromotionComponent],
  template: `
    <header class="top-bar">
      <a class="brand" routerLink="/">
        <img class="brand-logo" src="favicon.svg" alt="" width="32" height="32" />
        <span class="brand-name">Boussole</span>
      </a>
      <nav class="top-links">
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">{{ 'app.home' | transloco }}</a>
        <a routerLink="/ranks" routerLinkActive="active">{{ 'profile.ranks' | transloco }}</a>
      </nav>
      @if (stats.stats(); as current) {
        @let rank = current.culture;
        <a class="player-stats" routerLink="/profile" [attr.aria-label]="'stats.label' | transloco: { rank: rankName(rank.tier, rank.division), level: rank.level, points: points(current.points), streakDays: current.streakDays }">
          <app-rank-pill [rank]="rank" [points]="current.points">
            <span class="streak" [class.cold]="!current.studiedToday">
              <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2c1 4 5 5 5 10a5 5 0 01-10 0c0-2 1-3.5 2-4.5 0 2 1 3 2 3-1-3 0-6 1-8.5z"></path></svg>
              {{ current.streakDays }}
            </span>
          </app-rank-pill>
        </a>
      }
      <div class="top-actions">
        <app-settings-drawer />
      </div>
    </header>
    <router-outlet />
    @if (stats.shownPromotion(); as promotion) {
      <app-promotion [from]="promotion.from" [to]="promotion.to" [next]="nextStep()" [rank]="stats.stats()?.culture ?? null" [points]="stats.stats()?.points ?? 0" (closed)="stats.closePromotion()" />
    }
  `,
})
export class App {
  protected readonly stats = inject(StatsService);
  private readonly transloco = inject(TranslocoService);
  private readonly language = inject(LanguageService);

  protected rankName(tier: string, division: number): string {
    const name = this.transloco.translate('rank.' + tier, {}, this.language.current());
    return division > 0 ? `${name} ${roman(division)}` : name;
  }

  protected points(points: number): string {
    return formatPoints(points, this.language.current());
  }

  /** The division after the learner's, from the route the stats bring. */
  protected nextStep(): LadderStep | null {
    const current = this.stats.stats();
    if (!current) {
      return null;
    }
    const here = current.route.findIndex((step) => step.tier === current.culture.tier && step.division === Math.max(current.culture.division, 0));
    return here >= 0 ? (current.route[here + 1] ?? null) : null;
  }

  constructor() {
    inject(ServerSettingsService).load();
    this.stats.refresh();
  }
}

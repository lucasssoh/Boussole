import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LadderStep, Rank } from './core/api';
import { LanguageService } from './core/language.service';
import { RankEmblemComponent } from './shared/rank-emblem.component';
import { PromotionComponent } from './shared/promotion.component';
import { divisionProgress, formatPoints, roman, tierColors } from './shared/ranks';
import { ServerSettingsService } from './core/server-settings.service';
import { StatsService } from './core/stats.service';
import { SettingsDrawerComponent } from './core/settings-drawer.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TranslocoPipe, SettingsDrawerComponent, RankEmblemComponent, PromotionComponent],
  template: `
    <header class="top-bar">
      <a class="brand" routerLink="/">
        <img class="brand-logo" src="favicon.svg" alt="" width="32" height="32" />
        Boussole
      </a>
      @if (stats.stats(); as current) {
        @let rank = current.culture;
        <a class="player-stats" routerLink="/profile" [attr.aria-label]="'stats.label' | transloco: { rank: rankName(rank.tier, rank.division), level: rank.level, points: points(current.points), streakDays: current.streakDays }">
          <app-rank-emblem [tier]="rank.tier" [size]="28" />
          <span class="level-bar">
            <span class="level-line">{{ rankName(rank.tier, rank.division) }} · {{ 'stats.level' | transloco: { level: rank.level } }}</span>
            <span class="xp-track"><span class="xp-fill" [style.width.%]="100 * progress(rank, current.points)" [style.background]="color(rank.tier)"></span></span>
            <span class="level-points">{{ 'stats.points' | transloco: { points: points(current.points), next: points(ceil(rank.nextAt)) } }}</span>
          </span>
          <span class="streak" [class.cold]="!current.studiedToday">
            <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 2c1 4 5 5 5 10a5 5 0 01-10 0c0-2 1-3.5 2-4.5 0 2 1 3 2 3-1-3 0-6 1-8.5z"></path></svg>
            {{ current.streakDays }}
          </span>
        </a>
      }
      <nav class="top-actions">
        <app-settings-drawer />
      </nav>
    </header>
    <router-outlet />
    @if (stats.promotion(); as promotion) {
      <app-promotion [from]="promotion.from" [to]="promotion.to" [next]="nextStep()" (closed)="stats.closePromotion()" />
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

  protected ceil(points: number): number {
    return Math.ceil(points);
  }

  protected progress(rank: Rank, points: number): number {
    return divisionProgress(rank, points);
  }

  protected color(tier: string): string {
    return tierColors(tier)[0];
  }

  constructor() {
    inject(ServerSettingsService).load();
    this.stats.refresh();
  }
}

import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { emblemAmbiance, emblemInk } from '../shared/emblem';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { RankNames } from './rank-names';

/** One tier of the culture: where it starts, and what it asks past its points. */
interface Tier {
  tier: string;
  from: number;
  asks: string | null;
}

/** What the top culture tiers ask for, past their points. */
const ASKS: Readonly<Record<string, string>> = { sage: 'ranks.asksSage', 'grand-sage': 'ranks.asksGrandSage' };

/**
 * The tiers of the culture, from Curious to Grand Sage: each with its emblem, in its hour of the
 * day, what it stands for, and where it starts; the learner's with their division.
 */
@Component({
  selector: 'app-ranks-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide ranks">
      <p class="crumbs"><a routerLink="/profile">{{ 'profile.title' | transloco }}</a></p>
      <h1>{{ 'ranks.title' | transloco }}</h1>
      <ol class="tiers">
        @for (row of tiers(); track row.tier) {
          @let here = row.tier === culture()?.tier;
          <li class="tier-card" [class.here]="here" [style.background]="ambiance(row.tier)" [style.color]="ink(row.tier)">
            <app-rank-emblem [tier]="row.tier" [division]="here ? (culture()?.division ?? 0) : 0" [size]="104" [lively]="here" />
            <div class="tier-text">
              <h2>{{ names.rank(row.tier, 0) }}</h2>
              <p>{{ 'ranks.about.' + row.tier | transloco }}</p>
              <span class="tier-from">{{ 'ranks.since' | transloco: { points: names.points(row.from) } }}</span>
              @if (row.asks) {
                <span class="tier-asks">{{ row.asks | transloco }}</span>
              }
            </div>
          </li>
        }
      </ol>
    </main>
  `,
})
export class RanksPageComponent {
  protected readonly names = inject(RankNames);
  private readonly stats = inject(StatsService);
  private readonly steps = signal<LadderStep[]>([]);

  protected readonly culture = computed(() => this.stats.stats()?.culture ?? null);
  protected readonly tiers = computed<Tier[]>(() => {
    const found: Tier[] = [];
    for (const step of this.steps()) {
      if (found.at(-1)?.tier !== step.tier) {
        found.push({ tier: step.tier, from: step.from, asks: ASKS[step.tier] ?? null });
      }
    }
    return found;
  });

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.steps.set(ladders.culture), () => undefined);
    this.stats.refresh();
  }

  protected ambiance(tier: string): string {
    return emblemAmbiance(tier);
  }

  protected ink(tier: string): string {
    return emblemInk(tier);
  }
}

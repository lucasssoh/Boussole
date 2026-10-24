import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep, Ladders } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { roman } from '../shared/ranks';
import { RankNames } from './rank-names';

/** One tier of a ladder: its divisions, from V to I, or the top one alone. */
interface TierRow {
  tier: string;
  steps: LadderStep[];
  from: number;
  firstLevel: number;
  lastLevel: number | null;
  fields: number;
}

/** The fields the culture tiers ask for, past their points. */
const FIELDS: Readonly<Record<string, number>> = { sage: 3, 'grand-sage': 5 };

/** Both ladders, tier by tier: the culture, where the learner stands, and the specialty of a field. */
@Component({
  selector: 'app-ranks-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide ranks">
      <p class="crumbs"><a routerLink="/profile">{{ 'profile.title' | transloco }}</a></p>
      <h1>{{ 'ranks.title' | transloco }}</h1>
      <nav class="tabs">
        <button type="button" [class.active]="ladder() === 'culture'" (click)="ladder.set('culture')">{{ 'ranks.culture' | transloco }}</button>
        <button type="button" [class.active]="ladder() === 'specialty'" (click)="ladder.set('specialty')">{{ 'ranks.specialty' | transloco }}</button>
      </nav>
      <ol class="ladder">
        @for (row of rows(); track row.tier) {
          <li class="ladder-tier" [class.here]="row.tier === currentTier()" [class.ahead]="isAhead(row)">
            <app-rank-emblem [tier]="row.tier" [size]="42" />
            <div class="ladder-name">
              <strong>{{ names.rank(row.tier, 0) }}</strong>
              <span>
                @if (row.lastLevel) {
                  {{ 'ranks.levels' | transloco: { from: row.firstLevel, to: row.lastLevel } }}
                } @else {
                  {{ 'ranks.levelsFrom' | transloco: { from: row.firstLevel } }}
                }
              </span>
            </div>
            <div class="ladder-divisions">
              @for (step of row.steps; track step.firstLevel) {
                <span class="division-chip" [class.current]="isCurrent(step)" [title]="names.points(step.from)">{{ step.division > 0 ? roman(step.division) : '∞' }}</span>
              }
            </div>
            <div class="ladder-from">
              <strong>{{ 'ranks.from' | transloco: { points: names.points(row.from) } }}</strong>
              @if (row.fields > 0) {
                <span>{{ 'ranks.fields' | transloco: { count: row.fields } }}</span>
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
  protected readonly roman = roman;
  protected readonly ladder = signal<'culture' | 'specialty'>('culture');
  private readonly ladders = signal<Ladders | null>(null);

  protected readonly rows = computed<TierRow[]>(() => {
    const ladders = this.ladders();
    if (!ladders) {
      return [];
    }
    const steps = this.ladder() === 'culture' ? ladders.culture : ladders.specialty;
    const rows: TierRow[] = [];
    for (const step of steps) {
      const row = rows.at(-1);
      if (row && row.tier === step.tier) {
        row.steps.push(step);
      } else {
        rows.push({ tier: step.tier, steps: [step], from: step.from, firstLevel: step.firstLevel, lastLevel: null,
          fields: this.ladder() === 'culture' ? (FIELDS[step.tier] ?? 0) : 0 });
      }
    }
    rows.forEach((row, index) => (row.lastLevel = rows[index + 1] ? rows[index + 1].firstLevel - 1 : null));
    return rows;
  });
  protected readonly currentTier = computed(() => (this.ladder() === 'culture' ? this.stats.stats()?.culture.tier : undefined));

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.ladders.set(ladders), () => undefined);
    this.stats.refresh();
  }

  protected isCurrent(step: LadderStep): boolean {
    const rank = this.stats.stats()?.culture;
    return this.ladder() === 'culture' && !!rank && step.tier === rank.tier && step.division === Math.max(rank.division, 0);
  }

  protected isAhead(row: TierRow): boolean {
    const points = this.stats.stats()?.points ?? 0;
    return this.ladder() === 'culture' && row.from > points;
  }
}

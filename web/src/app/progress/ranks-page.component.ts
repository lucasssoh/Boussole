import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { FieldStanding, LadderStep, Ladders } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { divisionProgress, roman, tierColors } from '../shared/ranks';
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

/**
 * The tiers: the culture, overall, beside the specialties, one branch per field the learner has
 * points in, each with its tiers from Novice to Luminary and where the learner stands on it.
 */
@Component({
  selector: 'app-ranks-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide ranks">
      <p class="crumbs"><a routerLink="/profile">{{ 'profile.title' | transloco }}</a></p>
      <h1>{{ 'ranks.title' | transloco }}</h1>
      <div class="ranks-layout">
        <section class="ranks-culture">
          <h2>{{ 'ranks.culture' | transloco }}</h2>
          <ol class="ladder">
            @for (row of cultureRows(); track row.tier) {
              <li class="ladder-tier" [class.here]="row.tier === stats.stats()?.culture?.tier" [class.ahead]="row.from > points()">
                <app-rank-emblem [tier]="row.tier" [size]="34" />
                <div class="ladder-name">
                  <strong>{{ names.rank(row.tier, 0) }}</strong>
                  <span>{{ 'ranks.from' | transloco: { points: names.points(row.from) } }}@if (row.fields > 0) { · {{ 'ranks.fields' | transloco: { count: row.fields } }}}</span>
                </div>
                <div class="ladder-divisions">
                  @for (step of row.steps; track step.firstLevel) {
                    <span class="division-chip" [class.current]="isCurrent(step)" [title]="names.points(step.from)">{{ step.division > 0 ? roman(step.division) : '∞' }}</span>
                  }
                </div>
              </li>
            }
          </ol>
        </section>

        <section class="ranks-branches">
          <h2>{{ 'ranks.specialty' | transloco }}</h2>
          @for (field of fields(); track field.domain) {
            <article class="branch" [style.--tier]="color(field.specialty.tier)">
              <header class="branch-head">
                <app-rank-emblem [tier]="field.specialty.tier" [size]="40" />
                <div>
                  <strong>{{ names.field(field.domain) }}</strong>
                  <span class="branch-rank">{{ names.rank(field.specialty.tier, field.specialty.division) }} · {{ 'stats.level' | transloco: { level: field.specialty.level } }}</span>
                </div>
                <span class="branch-points">{{ 'profile.points' | transloco: { points: names.points(field.points) } }}</span>
              </header>
              <ol class="branch-track">
                @for (tier of specialtyTiers(); track tier.tier; let index = $index) {
                  <li [class]="'branch-node ' + nodeState(field, index)" [style.--node]="color(tier.tier)" [title]="names.rank(tier.tier, 0) + ' · ' + names.points(tier.from)">
                    <span class="branch-dot"></span>
                    <span class="branch-label">{{ names.rank(tier.tier, 0) }}</span>
                  </li>
                }
              </ol>
              <div class="branch-next">
                <span class="xp-track"><span class="xp-fill" [style.width.%]="100 * progress(field)" [style.background]="color(field.specialty.tier)"></span></span>
                <span>{{ 'ranks.nextDivision' | transloco: { points: names.points(toGo(field)) } }}</span>
              </div>
            </article>
          } @empty {
            <p class="note">{{ 'profile.noSpecialty' | transloco }}</p>
          }
        </section>
      </div>
    </main>
  `,
})
export class RanksPageComponent {
  protected readonly names = inject(RankNames);
  protected readonly stats = inject(StatsService);
  protected readonly roman = roman;
  private readonly ladders = signal<Ladders | null>(null);

  protected readonly points = computed(() => this.stats.stats()?.points ?? 0);
  protected readonly fields = computed<FieldStanding[]>(() => this.stats.stats()?.fields ?? []);
  protected readonly cultureRows = computed(() => rows(this.ladders()?.culture ?? [], true));
  /** The specialty tiers, each with where it starts. */
  protected readonly specialtyTiers = computed(() => rows(this.ladders()?.specialty ?? [], false));

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.ladders.set(ladders), () => undefined);
    this.stats.refresh();
  }

  protected isCurrent(step: LadderStep): boolean {
    const rank = this.stats.stats()?.culture;
    return !!rank && step.tier === rank.tier && step.division === Math.max(rank.division, 0);
  }

  protected nodeState(field: FieldStanding, index: number): string {
    const here = this.specialtyTiers().findIndex((tier) => tier.tier === field.specialty.tier);
    return index < here ? 'past' : index === here ? 'here' : 'next';
  }

  protected color(tier: string): string {
    return tierColors(tier)[0];
  }

  protected progress(field: FieldStanding): number {
    return divisionProgress(field.specialty, field.points);
  }

  protected toGo(field: FieldStanding): number {
    return Math.max(0, Math.ceil(field.specialty.nextAt - field.points));
  }
}

/** The steps of a ladder grouped by tier, with the levels each spans. */
function rows(steps: LadderStep[], culture: boolean): TierRow[] {
  const found: TierRow[] = [];
  for (const step of steps) {
    const row = found.at(-1);
    if (row && row.tier === step.tier) {
      row.steps.push(step);
    } else {
      found.push({ tier: step.tier, steps: [step], from: step.from, firstLevel: step.firstLevel, lastLevel: null,
        fields: culture ? (FIELDS[step.tier] ?? 0) : 0 });
    }
  }
  found.forEach((row, index) => (row.lastLevel = found[index + 1] ? found[index + 1].firstLevel - 1 : null));
  return found;
}

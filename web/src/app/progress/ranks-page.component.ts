import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { IconComponent } from '../shared/icon.component';
import { LoadingComponent } from '../shared/loading.component';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { CRITICAL_THINKING, isStrong } from '../shared/ranks';
import { RankNames } from './rank-names';

/** One tier of the culture: where it starts, and what it asks past its points. */
interface Tier {
  tier: string;
  from: number;
  asks: string | null;
}

/** What the top culture tiers ask for, past their points. */
const ASKS: Readonly<Record<string, string>> = { sage: 'ranks.asksSage', 'grand-sage': 'ranks.asksGrandSage' };

/** The size of an emblem in the row, and of the learner's, twice as large. */
const SIZE = 64;
const HERE_SIZE = 128;

/**
 * The tiers of the culture at a glance: one row of emblems linked by a line, the learner's twice as
 * large; under each, its points and, for Sage and Grand Sage, a mark for each specialty they ask.
 * The tier chosen, the learner's at first, says what it stands for below. On a phone, a tier a row.
 */
@Component({
  selector: 'app-ranks-page',
  imports: [IconComponent, LoadingComponent, RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide ranks">
      <a class="back" routerLink="/profile" [attr.aria-label]="'profile.title' | transloco" [title]="'profile.title' | transloco"><app-icon name="arrow-left" /></a>
      <h1>{{ 'ranks.title' | transloco }}</h1>
      @if (tiers().length === 0) {
        <app-loading />
      }
      <ol class="tier-row" [style.--line-top]="lineTop + 'px'" [style.--box]="boxHeight + 'px'">
        @for (row of tiers(); track row.tier; let index = $index) {
          @let here = row.tier === culture()?.tier;
          <li class="tier-cell" [class.here]="here" [class.passed]="index < hereIndex()" [class.ahead]="index > hereIndex()">
            <button type="button" [attr.aria-pressed]="row.tier === chosen()" (click)="choice.set(row.tier)">
              <span class="tier-mark">
                <span [style.margin-top.px]="here ? 0 : smallLift">
                  <app-rank-emblem [tier]="row.tier" [division]="here ? (culture()?.division ?? 0) : 0" [size]="here ? hereSize : size" [lively]="here" />
                </span>
              </span>
              <span class="tier-name">{{ names.rank(row.tier, 0) }}</span>
              <span class="tier-needs">
                <span class="tier-points">{{ 'ranks.points' | transloco: { points: names.points(row.from) } }}</span>
                @if (row.asks) {
                  <span class="tier-marks" role="img" [attr.aria-label]="row.asks | transloco" [title]="row.asks | transloco">
                    @for (met of specialtyMarks(); track $index) {
                      <span class="pip" [class.met]="met"></span>
                    }
                    @if (row.tier === 'grand-sage') {
                      <span class="pip critical" [class.met]="criticalThinking()"></span>
                    }
                  </span>
                }
              </span>
            </button>
          </li>
        }
      </ol>
      @if (chosen(); as tier) {
        <section class="tier-about" aria-live="polite">
          <p>{{ 'ranks.about.' + tier | transloco }}</p>
          <p class="tier-asks">{{ 'ranks.since' | transloco: { points: names.points(from(tier)) } }}@if (asks(tier); as text) { {{ text | transloco }}}.</p>
        </section>
      }
    </main>
  `,
})
export class RanksPageComponent {
  protected readonly names = inject(RankNames);
  private readonly stats = inject(StatsService);
  private readonly steps = signal<LadderStep[]>([]);

  protected readonly size = SIZE;
  protected readonly hereSize = HERE_SIZE;
  /** Every emblem with its gem centred on the line: the emblem box is 1.25 times as high as wide, its gem at 0.533 of it. */
  protected readonly boxHeight = HERE_SIZE * 1.25;
  protected readonly lineTop = Math.round(0.533 * HERE_SIZE * 1.25) - 2;
  protected readonly smallLift = Math.round(0.533 * (HERE_SIZE - SIZE) * 1.25);

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
  protected readonly hereIndex = computed(() => this.tiers().findIndex((row) => row.tier === this.culture()?.tier));
  /** The tier the learner chose to read about; theirs until they choose. */
  protected readonly choice = signal<string | null>(null);
  protected readonly chosen = computed(() => this.choice() ?? this.culture()?.tier ?? this.tiers()[0]?.tier ?? null);
  private readonly fields = computed(() => this.stats.stats()?.fields ?? []);
  /** The three specialties Sage asks for, those the learner has at Specialist or above first. */
  protected readonly specialtyMarks = computed(() => {
    const strong = this.fields().filter((field) => isStrong(field.specialty.tier) && field.domain !== CRITICAL_THINKING).length;
    return [0, 1, 2].map((index) => index < strong);
  });
  protected readonly criticalThinking = computed(() =>
    this.fields().some((field) => field.domain === CRITICAL_THINKING && isStrong(field.specialty.tier)),
  );

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.steps.set(ladders.culture), () => undefined);
    this.stats.refresh();
  }

  protected asks(tier: string): string | null {
    return ASKS[tier] ?? null;
  }

  protected from(tier: string): number {
    return this.tiers().find((row) => row.tier === tier)?.from ?? 0;
  }
}

import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { LadderStep } from '../core/api';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { tierColors } from '../shared/ranks';
import { RankNames } from './rank-names';

/** One emblem of the route, placed from the centre of the route. */
interface RouteItem {
  step: LadderStep;
  here: boolean;
  ahead: boolean;
  size: number;
  left: string;
  top: number;
}

/** How far from the centre an emblem is still drawn; the edges fade out before. */
const REACH = 760;

/**
 * The route of the culture: the emblem of every division in a row, the learner's always in the
 * middle and larger, the others smaller as they go away, those ahead greyed; behind them, a line
 * coloured with the tiers passed. Decorative besides its label: the rank is said elsewhere.
 */
@Component({
  selector: 'app-rank-route',
  imports: [RankEmblemComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'rank-route', '[style.height.px]': 'height()' },
  template: `
    <span class="route-track" [style.top.px]="cross() - 3"></span>
    <span class="route-fill" [style.top.px]="cross() - 3" [style.background]="fill()"></span>
    @for (item of items(); track item.step.firstLevel) {
      <span class="route-emblem" [class.ahead]="item.ahead" [style.left]="item.left" [style.top.px]="item.top" [title]="names.rank(item.step.tier, item.step.division)">
        <app-rank-emblem [tier]="item.step.tier" [division]="item.step.division" [size]="item.size" [lively]="item.here" />
      </span>
    }
    @if (current(); as step) {
      <span class="route-label" [style.top.px]="labelTop()">{{ names.rank(step.tier, step.division) }}</span>
    }
  `,
})
export class RankRouteComponent {
  readonly steps = input.required<LadderStep[]>();
  /** The index of the learner's division in the steps. */
  readonly here = input.required<number>();
  /** The size of an emblem next to the learner's, which is `zoom` times larger. */
  readonly base = input(56);
  readonly zoom = input(1.8);
  readonly height = input(190);

  protected readonly names = inject(RankNames);
  protected readonly current = computed(() => this.steps()[this.here()] ?? null);
  /** Where the line runs: the centre of the gems, leaving room for the label below. */
  protected readonly cross = computed(() => this.height() / 2 - 12);
  protected readonly labelTop = computed(() => this.cross() + this.base() * this.zoom() * 0.62);

  /** The centre of each emblem, from the learner's: each emblem with its ornaments next to the other. */
  private readonly centres = computed(() => {
    const steps = this.steps();
    const here = this.here();
    const sizes = steps.map((_, k) => this.sizeAt(k, here));
    const gem = (size: number) => size * 1.04;
    const centres: number[] = [];
    centres[here] = 0;
    for (let k = here + 1; k < steps.length; k++) centres[k] = centres[k - 1] + (gem(sizes[k - 1]) + gem(sizes[k])) / 2 + 6;
    for (let k = here - 1; k >= 0; k--) centres[k] = centres[k + 1] - (gem(sizes[k + 1]) + gem(sizes[k])) / 2 - 6;
    return { sizes, centres };
  });

  protected readonly items = computed<RouteItem[]>(() => {
    const { sizes, centres } = this.centres();
    const here = this.here();
    return this.steps()
      .map((step, k) => ({
        step, here: k === here, ahead: k > here, size: Math.round(sizes[k]),
        left: `calc(50% + ${(centres[k] - sizes[k] / 2).toFixed(1)}px)`,
        top: this.cross() - sizes[k] * 1.25 * 0.533,
      }))
      .filter((_, k) => Math.abs(centres[k]) < REACH);
  });

  /** The line up to the learner, in the colours of the tiers passed, each from its division V. */
  protected readonly fill = computed(() => {
    const { centres } = this.centres();
    const passed = this.steps()
      .slice(0, this.here() + 1)
      .map((step, k) => `${tierColors(step.tier)[0]} calc(50% + ${centres[k].toFixed(0)}px)`);
    return passed.length === 0 ? 'none' : `linear-gradient(90deg, ${passed.join(', ')}, transparent 50%)`;
  });

  private sizeAt(k: number, here: number): number {
    return k === here ? this.base() * this.zoom() : this.base() * Math.max(0.55, 1 - 0.09 * Math.abs(k - here));
  }
}

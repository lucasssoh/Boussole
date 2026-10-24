import { ChangeDetectionStrategy, Component, ElementRef, afterNextRender, computed, inject, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep, Rank } from '../core/api';
import { RankNames } from '../progress/rank-names';
import { RankEmblemComponent } from './rank-emblem.component';
import { RankPillComponent } from './rank-pill.component';
import { tierColors } from './ranks';

/** A rank reached: its tier and division. */
export interface Reached {
  tier: string;
  division: number;
}

/**
 * The moment a new division is reached, once the session is over: the screen dims and the emblem
 * alone stands over it. A new division: its numeral flies off and the new one falls in. A new
 * tier: the former emblem comes apart and the new one is put together piece by piece, with light
 * turning behind it, a word for the tier left, and the new pill. Escape or the button closes it.
 */
@Component({
  selector: 'app-promotion',
  imports: [RankEmblemComponent, RankPillComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'promotion',
    role: 'dialog',
    'aria-modal': 'true',
    '[class.new-tier]': 'newTier()',
    '[attr.aria-label]': 'title()',
    '[style.--tier]': 'colors()[0]',
    '(document:keydown.escape)': 'closed.emit()',
  },
  template: `
    <span class="promotion-label">{{ (newTier() ? 'promotion.tier' : 'promotion.division') | transloco }}</span>
    <div class="promotion-emblems">
      <span class="promotion-halo" aria-hidden="true"></span>
      @if (newTier()) {
        <span class="promotion-rays" aria-hidden="true"></span>
        <app-rank-emblem class="promotion-former" [tier]="from().tier" [division]="from().division" [size]="SIZE" motion="leave" />
        <app-rank-emblem [tier]="to().tier" [division]="to().division" [size]="SIZE" motion="assemble" [lively]="true" />
      } @else {
        <app-rank-emblem [tier]="to().tier" [division]="to().division" [size]="SIZE" [formerDivision]="from().division" [lively]="true" />
      }
    </div>
    <h2>{{ title() }}</h2>
    @if (newTier()) {
      <p class="promotion-leave">{{ 'promotion.leave.' + from().tier | transloco }}</p>
      @if (rank(); as reached) {
        <div class="promotion-pill">
          <span class="promotion-pill-label">{{ 'promotion.newPill' | transloco }}</span>
          <app-rank-pill [rank]="reached" [points]="points()" />
        </div>
      }
    }
    @if (next(); as step) {
      <p class="promotion-next">{{ 'promotion.next' | transloco: { rank: names.rank(step.tier, step.division), points: names.points(step.from) } }}</p>
    }
    <button type="button" class="primary" (click)="closed.emit()">{{ 'promotion.continue' | transloco }}</button>
  `,
})
export class PromotionComponent {
  readonly from = input.required<Reached>();
  readonly to = input.required<Reached>();
  readonly next = input<LadderStep | null>(null);
  /** The learner's rank and points, for the new pill. */
  readonly rank = input<Rank | null>(null);
  readonly points = input(0);
  readonly closed = output<void>();

  protected readonly SIZE = 150;
  protected readonly names = inject(RankNames);
  protected readonly newTier = computed(() => this.from().tier !== this.to().tier);
  protected readonly title = computed(() => this.names.rank(this.to().tier, this.to().division));
  protected readonly colors = computed(() => tierColors(this.to().tier));

  constructor() {
    const host = inject(ElementRef<HTMLElement>);
    afterNextRender(() => host.nativeElement.querySelector('button')?.focus());
  }
}

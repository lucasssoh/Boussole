import { ChangeDetectionStrategy, Component, ElementRef, afterNextRender, computed, inject, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { RankNames } from '../progress/rank-names';
import { emblemAmbiance, emblemInk } from './emblem';
import { RankEmblemComponent } from './rank-emblem.component';
import { tierColors } from './ranks';

/** A rank reached: its tier and division. */
export interface Reached {
  tier: string;
  division: number;
}

/**
 * The moment a new division is reached: the whole screen dims, the former emblem gives way to the
 * new one, with the next step ahead. Shown once per division; Escape or the button closes it.
 */
@Component({
  selector: 'app-promotion',
  imports: [RankEmblemComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'promotion', role: 'dialog', 'aria-modal': 'true', '[attr.aria-label]': 'title()', '(document:keydown.escape)': 'closed.emit()' },
  template: `
    <div class="promotion-card" [style.--tier]="colors()[0]" [style.--tier-deep]="colors()[1]">
      <span class="promotion-label">{{ (newTier() ? 'promotion.tier' : 'promotion.division') | transloco }}</span>
      <div class="promotion-emblems" [style.--ambiance]="ambiance()" [style.--stage-ink]="ink()">
        <span class="promotion-former"><app-rank-emblem [tier]="from().tier" [division]="from().division" [size]="56" /></span>
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"></path></svg>
        <span class="promotion-new"><app-rank-emblem [tier]="to().tier" [division]="to().division" [size]="112" [lively]="true" /></span>
      </div>
      <h2>{{ title() }}</h2>
      @if (newTier()) {
        <p class="promotion-leave">{{ 'promotion.leave.' + from().tier | transloco }}</p>
      }
      @if (next(); as step) {
        <p class="promotion-next">{{ 'promotion.next' | transloco: { rank: names.rank(step.tier, step.division), points: names.points(step.from) } }}</p>
      }
      <button type="button" class="primary" (click)="closed.emit()">{{ 'promotion.continue' | transloco }}</button>
    </div>
  `,
})
export class PromotionComponent {
  readonly from = input.required<Reached>();
  readonly to = input.required<Reached>();
  readonly next = input<LadderStep | null>(null);
  readonly closed = output<void>();

  protected readonly names = inject(RankNames);
  protected readonly newTier = computed(() => this.from().tier !== this.to().tier);
  protected readonly title = computed(() => this.names.rank(this.to().tier, this.to().division));
  protected readonly colors = computed(() => tierColors(this.to().tier));
  protected readonly ambiance = computed(() => emblemAmbiance(this.to().tier));
  protected readonly ink = computed(() => emblemInk(this.to().tier));

  constructor() {
    const host = inject(ElementRef<HTMLElement>);
    afterNextRender(() => host.nativeElement.querySelector('button')?.focus());
  }
}

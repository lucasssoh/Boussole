import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { Rank } from '../core/api';
import { LanguageService } from '../core/language.service';
import { pillFrame } from './pill-frame';
import { CULTURE_TIERS, SPECIALTY_TIERS, divisionProgress, formatPoints, ladderOrder } from './ranks';
import { RankEmblemComponent } from './rank-emblem.component';
import { SpecialtyEmblemComponent } from './specialty-emblem.component';

/** How long the body takes to fill before a new division, then before it starts again. */
const FILLED = 650;
const TURNED = 1300;

/**
 * A rank as a pill: its emblem, its level, and the bounds of its division, the body filling like a
 * gauge in a frame of its tier. When the points rise, it moves on its own: the level bounces; a
 * new division fills the body, a light crosses the frame, and the gauge starts again with the new
 * numeral, or a new emblem put together piece by piece. Nothing else on the screen moves.
 */
@Component({
  selector: 'app-rank-pill',
  imports: [RankEmblemComponent, SpecialtyEmblemComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'rank-pill',
    '[class.dark]': 'frame().dark',
    '[class.breathing]': 'frame().breathing',
    '[class.crossed]': 'crossed()',
    '[class.instant]': 'instant()',
    '[style]': 'style()',
  },
  template: `
    @let rank = shown();
    <span class="rank-pill-aura" aria-hidden="true"></span>
    @for (stud of frame().studs; track $index) {
      <span class="rank-pill-stud" [class.top]="stud.top" [style.left]="stud.left" aria-hidden="true"></span>
    }
    @if (frame().gem) {
      <span class="rank-pill-gem" aria-hidden="true"></span>
    }
    <span class="rank-pill-body">
      @if (rank) {
        @for (turn of [turns()]; track turn) {
          @if (ladder() === 'culture') {
            <app-rank-emblem [tier]="rank.tier" [division]="rank.division" [size]="emblemSize()" [formerDivision]="former()" [motion]="assembling() ? 'assemble' : 'still'" />
          } @else {
            <app-specialty-emblem [tier]="rank.tier" [size]="emblemSize() * 1.3" [class.arriving]="turn > 0" />
          }
        }
        <span class="rank-pill-text">
          <span class="rank-pill-level" [class.bumped]="bumped()">
            @if (field(); as name) {
              {{ 'stats.fieldLevel' | transloco: { field: name, level: rank.level } }}
            } @else {
              {{ 'stats.level' | transloco: { level: rank.level } }}
            }
          </span>
          <span class="rank-pill-bounds">{{ 'stats.bounds' | transloco: { from: amount(rank.divisionFrom), to: amount(rank.nextAt) } }}</span>
        </span>
      }
      <ng-content />
      <span class="rank-pill-shine" aria-hidden="true"><span></span></span>
    </span>
  `,
})
export class RankPillComponent {
  readonly ladder = input<'culture' | 'specialty'>('culture');
  readonly rank = input.required<Rank>();
  readonly points = input.required<number>();
  /** The field of a specialty, named before the level. */
  readonly field = input<string | null>(null);
  readonly emblemSize = input(30);

  private readonly language = inject(LanguageService);
  protected readonly shown = signal<Rank | null>(null);
  private readonly shownPoints = signal(0);
  protected readonly crossed = signal(false);
  protected readonly instant = signal(false);
  protected readonly bumped = signal(false);
  protected readonly former = signal<number | null>(null);
  protected readonly assembling = signal(false);
  /** Counts the emblems shown, so that a new one plays its arrival. */
  protected readonly turns = signal(0);
  private timers: ReturnType<typeof setTimeout>[] = [];

  protected readonly frame = computed(() => pillFrame(this.ladder(), this.shown()?.tier ?? this.rank().tier));
  protected readonly style = computed(() => {
    const rank = this.shown();
    return { ...this.frame().vars, '--share': String(rank ? divisionProgress(rank, this.shownPoints()) : 0) };
  });

  constructor() {
    effect(() => {
      const rank = this.rank();
      const points = this.points();
      untracked(() => this.arrive(rank, points));
    });
    inject(DestroyRef).onDestroy(() => this.timers.forEach(clearTimeout));
  }

  protected amount(value: number): string {
    return formatPoints(Math.ceil(value), this.language.current());
  }

  private arrive(rank: Rank, points: number): void {
    const before = this.shown();
    this.timers.forEach(clearTimeout);
    this.timers = [];
    if (!before || this.order(rank) < this.order(before)) {
      this.show(rank, points);
      return;
    }
    if (this.order(rank) === this.order(before)) {
      if (rank.level > before.level) {
        this.bump();
      }
      this.show(rank, points);
      return;
    }
    // A new division: the body fills, the frame shines, then the gauge starts again.
    this.shownPoints.set(before.nextAt);
    this.later(FILLED, () => this.crossed.set(true));
    this.later(TURNED, () => {
      this.instant.set(true);
      this.former.set(before.tier === rank.tier ? before.division : null);
      this.assembling.set(before.tier !== rank.tier);
      this.turns.update((turn) => turn + 1);
      this.shown.set(rank);
      this.shownPoints.set(rank.divisionFrom);
      this.bump();
      requestAnimationFrame(() => requestAnimationFrame(() => {
        this.instant.set(false);
        this.shownPoints.set(points);
      }));
      this.later(900, () => this.crossed.set(false));
      this.later(3600, () => {
        this.former.set(null);
        this.assembling.set(false);
      });
    });
  }

  private show(rank: Rank, points: number): void {
    this.shown.set(rank);
    this.shownPoints.set(points);
  }

  private bump(): void {
    this.bumped.set(false);
    requestAnimationFrame(() => this.bumped.set(true));
    this.later(600, () => this.bumped.set(false));
  }

  private order(rank: Rank): number {
    return ladderOrder(this.ladder() === 'culture' ? CULTURE_TIERS : SPECIALTY_TIERS, rank);
  }

  private later(delay: number, action: () => void): void {
    this.timers.push(setTimeout(action, delay));
  }
}

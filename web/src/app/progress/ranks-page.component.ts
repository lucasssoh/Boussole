import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, afterNextRender, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
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

/**
 * The tiers of the culture, from Curious to Grand Sage, in one light frame: a band per tier, its
 * emblem on one side and the other in turn, what it stands for and what it asks, the emblems linked
 * by a winding trail; the learner's tier ringed.
 */
@Component({
  selector: 'app-ranks-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide">
    <p class="crumbs"><a routerLink="/profile">{{ 'profile.title' | transloco }}</a></p>
    <h1>{{ 'ranks.title' | transloco }}</h1>
    <div class="tier-frame" #frame>
    <svg class="tier-trail" aria-hidden="true">
      <defs>
        <linearGradient id="tier-trail-ink" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#9fb0bf"></stop>
          <stop offset="1" stop-color="#0b7a5f"></stop>
        </linearGradient>
      </defs>
      <path [attr.d]="trail()" fill="none" stroke="url(#tier-trail-ink)" stroke-width="4" stroke-dasharray="2 12" stroke-linecap="round"></path>
    </svg>
      <ol class="tiers">
        @for (row of tiers(); track row.tier; let index = $index) {
          <li class="tier-band" [class.right]="index % 2 === 1" [class.here]="row.tier === culture()?.tier">
            <div class="tier-mark">
              <app-rank-emblem [tier]="row.tier" [size]="136" [lively]="row.tier === culture()?.tier" />
            </div>
            <div class="tier-text">
              <h2>{{ names.rank(row.tier, 0) }}</h2>
              <p>{{ 'ranks.about.' + row.tier | transloco }}</p>
              <div class="tier-needs">
                <span class="need" [title]="'ranks.since' | transloco: { points: names.points(row.from) }">
                  <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true"><path d="M8 1 L14 4.5 V11.5 L8 15 L2 11.5 V4.5 Z"></path></svg>
                  {{ 'ranks.points' | transloco: { points: names.points(row.from) } }}
                </span>
                @if (row.asks) {
                  <span class="need" role="img" [attr.aria-label]="row.asks | transloco" [title]="row.asks | transloco">
                    <span class="need-plus" aria-hidden="true">+</span>
                    @for (met of specialtyMarks(); track $index) {
                      <span class="pip" [class.met]="met"></span>
                    }
                    {{ names.rank('specialist', 0) }}
                  </span>
                  @if (row.tier === 'grand-sage') {
                    <span class="need" aria-hidden="true">
                      <span class="need-plus">+</span>
                      <span class="pip critical" [class.met]="criticalThinking()"></span>
                      {{ names.field('critical-thinking') }}
                    </span>
                  }
                }
              </div>
            </div>
          </li>
        }
      </ol>
    </div>
    </main>
  `,
})
export class RanksPageComponent {
  protected readonly names = inject(RankNames);
  private readonly stats = inject(StatsService);
  private readonly steps = signal<LadderStep[]>([]);
  private readonly frame = viewChild.required<ElementRef<HTMLElement>>('frame');

  protected readonly culture = computed(() => this.stats.stats()?.culture ?? null);
  private readonly fields = computed(() => this.stats.stats()?.fields ?? []);
  /** The three specialties Sage asks for, those the learner has at Specialist or above first. */
  protected readonly specialtyMarks = computed(() => {
    const strong = this.fields().filter((field) => isStrong(field.specialty.tier) && field.domain !== CRITICAL_THINKING).length;
    return [0, 1, 2].map((index) => index < strong);
  });
  protected readonly criticalThinking = computed(() =>
    this.fields().some((field) => field.domain === CRITICAL_THINKING && isStrong(field.specialty.tier)),
  );
  protected readonly tiers = computed<Tier[]>(() => {
    const found: Tier[] = [];
    for (const step of this.steps()) {
      if (found.at(-1)?.tier !== step.tier) {
        found.push({ tier: step.tier, from: step.from, asks: ASKS[step.tier] ?? null });
      }
    }
    return found;
  });
  /** The winding trail between the emblems, drawn once they are laid out. */
  protected readonly trail = signal('');

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.steps.set(ladders.culture), () => undefined);
    this.stats.refresh();
    const observer = new ResizeObserver(() => this.drawTrail());
    afterNextRender(() => observer.observe(this.frame().nativeElement));
    inject(DestroyRef).onDestroy(() => observer.disconnect());
  }

  /** Each stretch leaves an emblem downward and reaches the next from above. */
  private drawTrail(): void {
    const frame = this.frame().nativeElement;
    const origin = frame.getBoundingClientRect();
    const points = [...frame.querySelectorAll('.tier-mark')].map((mark) => {
      const box = mark.getBoundingClientRect();
      return [box.left + box.width / 2 - origin.left, box.top + box.height / 2 - origin.top];
    });
    const path = points.map(([x, y], index) => {
      if (index === 0) {
        return `M${x} ${y}`;
      }
      const [px, py] = points[index - 1];
      const bend = (y - py) * 0.62;
      return ` C${px} ${py + bend} ${x} ${y - bend} ${x} ${y}`;
    });
    this.trail.set(path.join(''));
  }
}

import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, afterNextRender, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { ALTITUDE, NIGHT, emblemStep } from '../shared/emblem';
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

/** Where the bands start, in percent of the sky's height, below the title. */
const TOP = 8;

/** A point of the sky, in percent of its width and height. */
interface Speck {
  x: number;
  y: number;
  r: number;
  opacity: number;
  gold: boolean;
  twinkle: string | null;
}

/**
 * The tiers of the culture as one climb, from the haze near the ground to the black of space: a band
 * per tier, its emblem on one side and the other in turn, what it stands for and what it asks, the
 * emblems linked by a winding trail; the learner's tier ringed. Stars appear high in the blue and
 * crowd the night.
 */
@Component({
  selector: 'app-ranks-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'sky', '[style.--sky]': 'sky()' },
  template: `
    <svg class="sky-layer" aria-hidden="true">
      <defs>
        <filter id="sky-cloud" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="18"></feGaussianBlur></filter>
        <filter id="sky-nebula" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="60"></feGaussianBlur></filter>
        <linearGradient id="sky-trail" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#4f6f86" stop-opacity="0.45"></stop>
          <stop offset="0.5" stop-color="#ffffff" stop-opacity="0.5"></stop>
          <stop offset="1" stop-color="#fff3c4" stop-opacity="0.6"></stop>
        </linearGradient>
      </defs>
      @for (cloud of clouds; track $index) {
        <ellipse [attr.cx]="cloud.x + '%'" [attr.cy]="cloud.y + '%'" [attr.rx]="cloud.rx" [attr.ry]="cloud.ry" fill="#ffffff" [attr.opacity]="cloud.opacity" filter="url(#sky-cloud)"></ellipse>
      }
      <ellipse cx="76%" cy="88%" rx="360" ry="200" fill="#7048e8" opacity="0.35" filter="url(#sky-nebula)"></ellipse>
      <ellipse cx="24%" cy="95%" rx="320" ry="160" fill="#1098ad" opacity="0.28" filter="url(#sky-nebula)"></ellipse>
      <ellipse cx="60%" cy="81%" rx="260" ry="120" fill="#c2255c" opacity="0.18" filter="url(#sky-nebula)"></ellipse>
      @for (star of stars; track $index) {
        <circle [class.twinkle]="!!star.twinkle" [style.--twinkle]="star.twinkle" [attr.cx]="star.x + '%'" [attr.cy]="star.y + '%'" [attr.r]="star.r" [attr.fill]="star.gold ? '#fff3c4' : '#ffffff'" [attr.opacity]="star.opacity"></circle>
      }
      <path [attr.d]="trail()" fill="none" stroke="url(#sky-trail)" stroke-width="4" stroke-dasharray="2 12" stroke-linecap="round"></path>
    </svg>
    <main class="sky-inner">
      <p class="crumbs"><a routerLink="/profile">{{ 'profile.title' | transloco }}</a></p>
      <h1>{{ 'ranks.title' | transloco }}</h1>
      <ol class="tiers">
        @for (row of tiers(); track row.tier; let index = $index) {
          <li class="tier-band" [class.right]="index % 2 === 1" [class.night]="night(row.tier)" [class.here]="row.tier === culture()?.tier">
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
    </main>
  `,
})
export class RanksPageComponent {
  protected readonly names = inject(RankNames);
  private readonly stats = inject(StatsService);
  private readonly steps = signal<LadderStep[]>([]);
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;

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
  /** The sky, the height of each tier at the middle of its band. */
  protected readonly sky = computed(() => {
    const stops = ALTITUDE.map((color, index) => `${color} ${(TOP + ((index + 0.5) * (100 - TOP)) / ALTITUDE.length).toFixed(1)}%`);
    return `linear-gradient(180deg, #fdf8ee 0%, ${stops.join(', ')}, #000000 100%)`;
  });
  /** The winding trail between the emblems, drawn once they are laid out. */
  protected readonly trail = signal('');
  protected readonly clouds = CLOUDS;
  protected readonly stars = STARS;

  constructor() {
    inject(BoussoleApi).ranks().then((ladders) => this.steps.set(ladders.culture), () => undefined);
    this.stats.refresh();
    const observer = new ResizeObserver(() => this.drawTrail());
    afterNextRender(() => observer.observe(this.host));
    inject(DestroyRef).onDestroy(() => observer.disconnect());
  }

  protected night(tier: string): boolean {
    return emblemStep(tier) >= NIGHT;
  }

  /** Each stretch leaves an emblem downward and reaches the next from above. */
  private drawTrail(): void {
    const origin = this.host.getBoundingClientRect();
    const points = [...this.host.querySelectorAll('.tier-mark')].map((mark) => {
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

/** Haze near the ground: where, how wide, how thick. */
const CLOUDS = [
  [14, 9, 220, 40, 0.8], [82, 12, 260, 46, 0.7], [50, 19, 300, 38, 0.5], [17, 28, 240, 34, 0.45], [86, 34, 220, 30, 0.35], [40, 42, 200, 26, 0.22],
].map(([x, y, rx, ry, opacity]) => ({ x, y, rx, ry, opacity }));

/** Stars appear high in the blue, then crowd the night: how many in each of the four highest bands. */
const STARS: Speck[] = (() => {
  let seed = 7;
  const random = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
  const specks: Speck[] = [];
  for (const [band, count] of [[4, 6], [5, 26], [6, 60], [7, 110]]) {
    for (let k = 0; k < count; k++) {
      const y = TOP + ((band - 0.4 + random() * 1.4) * (100 - TOP)) / 8;
      specks.push({
        x: +(random() * 100).toFixed(2), y: +Math.min(99.5, y).toFixed(2), r: +(0.6 + random() * (band === 7 ? 1.8 : 1.3)).toFixed(1),
        opacity: +(band === 4 ? 0.35 : 0.5 + random() * 0.5).toFixed(2), gold: random() < 0.15,
        twinkle: random() < 0.2 ? `${(2 + random() * 3).toFixed(1)}s` : null,
      });
    }
  }
  return specks;
})();

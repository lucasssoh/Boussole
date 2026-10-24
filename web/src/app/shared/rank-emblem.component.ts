import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { HEX, LEAVES, MOTES, RAYS, RIVETS, STEMS, emblemLook, numeralPath } from './emblem';
import { roman } from './ranks';

let emblems = 0;

/**
 * The emblem of a rank: a gem around an instrument of navigation, ornamented tier after tier, and
 * its division in relief. The aura and ornaments overflow the box. Lively, the light moves (the
 * aura breathes, a sheen crosses the gem), unless the system asks for less motion. Its pieces (aura,
 * ring, laurel, gem, rivets, dial, instrument, lamp, numeral) can come apart and together. Decorative.
 */
@Component({
  selector: 'app-rank-emblem',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let l = look();
    @let s = l.step;
    <svg [attr.width]="size()" [attr.height]="size() * 1.25" viewBox="-4 -12 72 90" aria-hidden="true" [class.lively]="lively()" [class.moving]="moving()">
      <defs>
        <linearGradient [attr.id]="id + '-face'" x1="0" y1="0" x2="0.35" y2="1">
          <stop offset="0" [attr.stop-color]="l.light"></stop>
          <stop offset="0.45" [attr.stop-color]="l.color"></stop>
          <stop offset="1" [attr.stop-color]="l.deep"></stop>
        </linearGradient>
        <linearGradient [attr.id]="id + '-metal'" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#fffbe6"></stop>
          <stop offset="0.5" [attr.stop-color]="l.ornament"></stop>
          <stop offset="1" [attr.stop-color]="l.ornamentDeep"></stop>
        </linearGradient>
        <linearGradient [attr.id]="id + '-leaf'" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" [attr.stop-color]="l.leafLight"></stop>
          <stop offset="1" [attr.stop-color]="l.leafDeep"></stop>
        </linearGradient>
        <radialGradient [attr.id]="id + '-disc'" cx="0.38" cy="0.32" r="0.75">
          <stop offset="0" stop-color="#ffffff"></stop>
          <stop offset="0.45" [attr.stop-color]="l.disc"></stop>
          <stop offset="1" [attr.stop-color]="l.discEdge"></stop>
        </radialGradient>
        <linearGradient [attr.id]="id + '-sheen'" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stop-color="#ffffff" stop-opacity="0"></stop>
          <stop offset="0.35" stop-color="#ffffff" stop-opacity="0.06"></stop>
          <stop offset="0.5" stop-color="#ffffff" stop-opacity="0.38"></stop>
          <stop offset="0.65" stop-color="#ffffff" stop-opacity="0.06"></stop>
          <stop offset="1" stop-color="#ffffff" stop-opacity="0"></stop>
        </linearGradient>
        <radialGradient [attr.id]="id + '-aura'" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0" [attr.stop-color]="l.auraColor"></stop>
          <stop offset="0.6" [attr.stop-color]="l.auraColor" stop-opacity="0.55"></stop>
          <stop offset="1" [attr.stop-color]="l.auraColor" stop-opacity="0"></stop>
        </radialGradient>
        <radialGradient [attr.id]="id + '-aura2'" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0" [attr.stop-color]="l.auraLight" stop-opacity="0.9"></stop>
          <stop offset="0.7" [attr.stop-color]="l.auraLight" stop-opacity="0.3"></stop>
          <stop offset="1" [attr.stop-color]="l.auraLight" stop-opacity="0"></stop>
        </radialGradient>
        <radialGradient [attr.id]="id + '-gloss'" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0" stop-color="#ffffff" stop-opacity="0.75"></stop>
          <stop offset="0.5" stop-color="#ffffff" stop-opacity="0.25"></stop>
          <stop offset="1" stop-color="#ffffff" stop-opacity="0"></stop>
        </radialGradient>
        <clipPath [attr.id]="id + '-clip'"><path [attr.d]="hex" transform="translate(32 36) scale(0.86) translate(-32 -36)"></path></clipPath>
        <filter [attr.id]="id + '-glow'" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur [attr.stdDeviation]="l.blur"></feGaussianBlur></filter>
        <filter [attr.id]="id + '-shadow'" x="-30%" y="-30%" width="160%" height="160%"><feDropShadow dx="0" dy="2.2" stdDeviation="1.6" flood-color="#0f1c1f" flood-opacity="0.32"></feDropShadow></filter>
        <filter [attr.id]="id + '-lift'" x="-30%" y="-40%" width="160%" height="180%"><feDropShadow dx="0" dy="1.4" stdDeviation="1" flood-color="#0f1c1f" flood-opacity="0.45"></feDropShadow></filter>
      </defs>

      <g class="part part-aura">
      @if (s >= 1) {
        <g class="emblem-aura" [class.breathing]="s >= 5" [attr.opacity]="l.auraOpacity" [style.--aura-low]="l.auraLow" [style.--aura-high]="l.auraHigh" [attr.filter]="url('glow')">
          <ellipse cx="32" cy="37" [attr.rx]="l.auraR" [attr.ry]="l.auraRy" [attr.fill]="url('aura')"></ellipse>
          <g [class.emblem-swirl]="s >= 5">
            <ellipse cx="27" cy="31" [attr.rx]="l.aura2R" [attr.ry]="l.aura2Ry" [attr.fill]="url('aura2')"></ellipse>
            <ellipse cx="38" cy="42" [attr.rx]="l.aura2Ry" [attr.ry]="l.aura2R" [attr.fill]="url('aura')" opacity="0.6"></ellipse>
          </g>
        </g>
      }
      </g>
      <g class="part part-ring">
      @if (s >= 7) {
        <circle cx="32" cy="36" r="46" fill="none" [attr.stroke]="url('metal')" stroke-width="2.2" stroke-dasharray="2 5"></circle>
      }
      @if (s >= 6) {
        @for (ray of rays; track $index) {
          <polygon [attr.points]="ray" [attr.fill]="url('metal')" opacity="0.5"></polygon>
        }
      }
      @if (s >= 4) {
        <circle cx="32" cy="36" r="40" fill="none" [attr.stroke]="url('metal')" stroke-width="2.6"></circle>
        <circle cx="32" cy="36" r="36.6" fill="none" [attr.stroke]="l.ornamentDeep" stroke-width="3.4" stroke-dasharray="0.7 3.1" opacity="0.8"></circle>
      }
      </g>

      <g [attr.filter]="moving() ? null : url('shadow')">
        @if (s >= 3) {
          <g class="part part-laurel">
          <path [attr.d]="stems" fill="none" [attr.stroke]="l.leafDeep" stroke-width="1.3" stroke-linecap="round"></path>
          @for (leaf of leaves; track $index) {
            <path d="M0 0 Q3.2 -4.6 0 -10 Q-3.2 -4.6 0 0 Z" [attr.transform]="'translate(' + leaf.x + ' ' + leaf.y + ') rotate(' + leaf.a + ')'" [attr.fill]="url('leaf')" [attr.stroke]="l.leafDeep" stroke-width="0.5"></path>
          }
          </g>
        }
        <g class="part part-body">
        <path [attr.d]="hex" [attr.fill]="l.deep" [attr.stroke]="l.deep" stroke-width="1.5" stroke-linejoin="round"></path>
        <path [attr.d]="hex" [attr.fill]="url('face')" transform="translate(32 36) scale(0.86) translate(-32 -36)"></path>
        <path d="M32 9.5 L32 17 M55 22.7 L47.6 26.6 M55 49.3 L47.6 45.4 M32 62.5 L32 55 M9 49.3 L16.4 45.4 M9 22.7 L16.4 26.6" stroke="#ffffff" stroke-width="1" opacity="0.4"></path>
        <path d="M11 22 L32 9.8 L53 22" fill="none" stroke="#ffffff" stroke-width="1.5" stroke-linecap="round" opacity="0.55"></path>
        </g>
        @if (s >= 1) {
          <path class="part part-trim" [attr.d]="hex" fill="none" [attr.stroke]="l.trimColor" stroke-width="1.4" opacity="0.85" transform="translate(32 36) scale(0.74) translate(-32 -36)"></path>
        }
        @if (s >= 2) {
          <g class="part part-rivets">
          @for (rivet of rivets; track $index) {
            <circle [attr.cx]="rivet.x" [attr.cy]="rivet.y" r="1.8" [attr.fill]="url('metal')" [attr.stroke]="l.deep" stroke-width="0.5"></circle>
          }
          </g>
        }
        @if (s >= 4) {
          <g class="part part-body" [attr.clip-path]="url('clip')">
            <g class="emblem-sheen" [style.--flow]="l.flow">
              <rect x="-30" y="-10" width="26" height="100" [attr.fill]="url('sheen')" transform="skewX(-20)"></rect>
            </g>
          </g>
        }
        <g class="part part-disc">
          <circle cx="32" cy="36" r="17.6" [attr.fill]="l.deep"></circle>
          <circle cx="32" cy="36" r="16" [attr.fill]="url('disc')"></circle>
        </g>
        <g class="part part-needle">
        <g transform="translate(16 20) scale(0.32)" stroke-linejoin="round">
          @switch (s) {
            @case (0) {
              <circle cx="50" cy="50" r="30" fill="none" stroke="#74c0fc" stroke-width="3"></circle>
              <ellipse cx="50" cy="50" rx="9" ry="6" fill="#d49a62" [attr.stroke]="l.deep" stroke-width="3"></ellipse>
              <line x1="22" y1="38" x2="50" y2="50" stroke="#e03131" stroke-width="7" stroke-linecap="round"></line>
              <line x1="50" y1="50" x2="78" y2="62" [attr.stroke]="l.deep" stroke-width="7" stroke-linecap="round"></line>
            }
            @case (1) {
              <path d="M50 8 V18 M50 82 V92 M8 50 H18 M82 50 H92" [attr.stroke]="l.deep" stroke-width="6" stroke-linecap="round"></path>
              <polygon points="50,16 58,50 42,50" fill="#e03131" [attr.stroke]="l.deep" stroke-width="3.4"></polygon>
              <polygon points="50,84 58,50 42,50" fill="#ffffff" [attr.stroke]="l.deep" stroke-width="3.4"></polygon>
            }
            @case (2) {
              <polygon points="25,25 52,46 75,25 54,52 75,75 48,54 25,75 46,48" [attr.fill]="l.light" [attr.stroke]="l.deep" stroke-width="2.6"></polygon>
              <polygon points="50,8 56,44 92,50 56,56 50,92 44,56 8,50 44,44" [attr.fill]="l.color" [attr.stroke]="l.deep" stroke-width="3.2"></polygon>
              <polygon points="50,8 56,44 44,44" fill="#e03131"></polygon>
            }
            @case (3) {
              <circle cx="50" cy="50" r="36" fill="none" [attr.stroke]="l.color" stroke-width="9"></circle>
              <circle cx="50" cy="50" r="36" fill="none" [attr.stroke]="l.deep" stroke-width="2.6"></circle>
              <rect x="44" y="6" width="12" height="12" rx="2" [attr.fill]="l.deep"></rect>
              <rect x="44" y="82" width="12" height="12" rx="2" [attr.fill]="l.deep"></rect>
              <polygon points="50,22 55,45 78,50 55,55 50,78 45,55 22,50 45,45" [attr.fill]="l.color" [attr.stroke]="l.deep" stroke-width="2.6"></polygon>
              <polygon points="50,22 55,45 45,45" fill="#e03131"></polygon>
            }
            @case (4) {
              <path d="M50 26 L12 52 M50 26 L20 70 M50 26 L34 80 M50 26 L50 84 M50 26 L66 80 M50 26 L80 70 M50 26 L88 52" [attr.stroke]="l.color" stroke-width="3"></path>
              <polygon points="50,26 50,8 62,26" [attr.fill]="l.deep"></polygon>
              <circle cx="50" cy="62" r="14" fill="#ffffff" [attr.stroke]="l.deep" stroke-width="3.4"></circle>
              <line x1="50" y1="50" x2="50" y2="62" stroke="#e03131" stroke-width="5" stroke-linecap="round"></line>
            }
            @case (5) {
              <circle cx="50" cy="50" r="34" fill="none" stroke="#d4a017" stroke-width="10"></circle>
              <path d="M50 16 V84 M16 50 H84" stroke="#d4a017" stroke-width="9"></path>
              <g transform="rotate(-30 50 50)">
                <rect x="12" y="45" width="76" height="10" rx="2" [attr.fill]="l.color" [attr.stroke]="l.deep" stroke-width="2.6"></rect>
                <rect x="16" y="35" width="7" height="30" rx="1.5" [attr.fill]="l.color" [attr.stroke]="l.deep" stroke-width="2.2"></rect>
                <rect x="77" y="35" width="7" height="30" rx="1.5" [attr.fill]="l.color" [attr.stroke]="l.deep" stroke-width="2.2"></rect>
              </g>
            }
            @case (6) {
              <circle cx="50" cy="50" r="36" fill="none" [attr.stroke]="l.deep" stroke-width="5" stroke-dasharray="1.4 3.4"></circle>
              <circle cx="57" cy="45" r="21" fill="none" [attr.stroke]="l.color" stroke-width="6"></circle>
              <path d="M50 50 Q36 34 24 24 M50 50 Q66 32 78 24 M50 50 Q30 64 18 72 M50 50 Q66 72 70 84" fill="none" [attr.stroke]="l.color" stroke-width="5" stroke-linecap="round"></path>
              <line x1="10" y1="68" x2="90" y2="32" [attr.stroke]="l.deep" stroke-width="6" stroke-linecap="round"></line>
            }
            @default {
              <circle cx="50" cy="50" r="34" fill="none" stroke="#f2c94c" stroke-width="5"></circle>
              <ellipse cx="50" cy="50" rx="34" ry="9" fill="none" stroke="#f2c94c" stroke-width="4"></ellipse>
              <ellipse cx="50" cy="50" rx="34" ry="15" fill="none" stroke="#fff3c4" stroke-width="6" stroke-dasharray="2.4 2.6" transform="rotate(23.4 50 50)"></ellipse>
              <ellipse cx="50" cy="50" rx="11" ry="34" fill="none" stroke="#f2c94c" stroke-width="3"></ellipse>
              <circle cx="50" cy="50" r="8" fill="#f2c94c"></circle>
            }
          }
        </g>
        </g>
        <g class="part part-disc">
        <path d="M59.71 20 L59.71 52 L32 68" fill="none" [attr.stroke]="l.light" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round" opacity="0.35" transform="translate(32 36) scale(0.82) translate(-32 -36)"></path>
        <ellipse cx="26" cy="28" rx="9" ry="4.6" [attr.fill]="url('gloss')" transform="rotate(-30 26 28)"></ellipse>
        <circle cx="23.2" cy="25.6" r="1" fill="#ffffff" opacity="0.85"></circle>
        </g>
        @if (s >= 5) {
          <g class="part part-lamp">
          <path d="M32 -10.5 C29.6 -7.4 29.4 -5.2 32 -3.6 C34.6 -5.2 34.4 -7.4 32 -10.5 Z" fill="#ffd43b" stroke="#f08c00" stroke-width="0.7"></path>
          <path d="M32 -8.2 C31 -6.8 31 -5.8 32 -5 C33 -5.8 33 -6.8 32 -8.2 Z" fill="#fff9db"></path>
          <path d="M23 1 Q32 7 41 1 Q38 -3 32 -3 Q26 -3 23 1 Z" [attr.fill]="url('metal')" [attr.stroke]="l.ornamentDeep" stroke-width="0.9"></path>
          <path d="M41 1 Q45 -1 46.5 -3.5" fill="none" [attr.stroke]="l.ornamentDeep" stroke-width="1.4" stroke-linecap="round"></path>
          </g>
        }
      </g>

      @if (formerNumeral(); as glyphs) {
        <g class="part part-former-numeral" stroke-width="1.8" stroke-linejoin="round">
          <path [attr.d]="glyphs" [attr.fill]="l.deep" [attr.stroke]="l.deep" transform="translate(0 2.2)"></path>
          <path [attr.d]="glyphs" [attr.fill]="l.mid" [attr.stroke]="l.mid" transform="translate(0 0.8)"></path>
          <path [attr.d]="glyphs" [attr.fill]="url('face')" [attr.stroke]="url('face')"></path>
        </g>
      }
      @if (numeral(); as glyphs) {
        <g class="part part-numeral" [attr.filter]="moving() ? null : url('lift')" stroke-width="1.8" stroke-linejoin="round">
          <path [attr.d]="glyphs" [attr.fill]="l.deep" [attr.stroke]="l.deep" transform="translate(0 2.2)"></path>
          <path [attr.d]="glyphs" [attr.fill]="l.deep" [attr.stroke]="l.deep" transform="translate(0 1.5)"></path>
          <path [attr.d]="glyphs" [attr.fill]="l.mid" [attr.stroke]="l.mid" transform="translate(0 0.8)"></path>
          <path [attr.d]="glyphs" [attr.fill]="url('face')" [attr.stroke]="url('face')"></path>
          <path [attr.d]="glyphs" fill="none" stroke="#ffffff" stroke-width="0.6" opacity="0.45" transform="translate(-0.2 -0.35)"></path>
        </g>
      }
      @if (s >= 6) {
        <g class="part part-aura">
        @for (mote of motes; track $index) {
          <circle class="emblem-mote" [attr.cx]="mote.x" [attr.cy]="mote.y" [attr.r]="mote.r" [attr.fill]="l.auraLight" [style.--duration]="mote.duration" [style.--delay]="mote.delay"></circle>
        }
        </g>
      }
    </svg>
  `,
  host: {
    class: 'rank-emblem',
    '[class.assembling]': "motion() === 'assemble'",
    '[class.leaving]': "motion() === 'leave'",
    '[class.renumbering]': '!!formerNumeral()',
  },
})
export class RankEmblemComponent {
  readonly tier = input.required<string>();
  /** The division, V (5) to I (1); 0 shows none, as for the top tier or a tier as a whole. */
  readonly division = input(0);
  readonly size = input(28);
  readonly lively = input(false);
  /**
   * How the emblem comes: still; put together piece by piece (a new tier); or taken apart (the
   * tier left). The pieces are the same in every emblem, so every tier comes and goes the same way.
   */
  readonly motion = input<'still' | 'assemble' | 'leave'>('still');
  /** The division just left in the same tier: its numeral flies off and the new one falls in. */
  readonly formerDivision = input<number | null>(null);

  protected readonly id = 'emblem-' + emblems++;
  protected readonly hex = HEX;
  protected readonly rivets = RIVETS;
  protected readonly rays = RAYS;
  protected readonly stems = STEMS;
  protected readonly leaves = LEAVES;
  protected readonly motes = MOTES;
  protected readonly look = computed(() => emblemLook(this.tier()));
  protected readonly numeral = computed(() => {
    const division = this.division();
    return division > 0 && this.look().step < 7 ? numeralPath(roman(division)) : '';
  });

  protected readonly formerNumeral = computed(() => {
    const former = this.formerDivision();
    return former && former !== this.division() && this.look().step < 7 ? numeralPath(roman(former)) : '';
  });
  /** While moving, the drop shadows of the SVG give way to one of CSS, which follows the pieces. */
  protected readonly moving = computed(() => this.motion() !== 'still' || !!this.formerNumeral());

  protected url(part: string): string {
    return `url(#${this.id}-${part})`;
  }
}

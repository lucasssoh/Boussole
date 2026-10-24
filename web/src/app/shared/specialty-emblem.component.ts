import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** The metal of each specialty tier, from iron to crystal: metal, deep, light, then the glass and its edge. */
const METALS: Readonly<Record<string, { step: number; colors: readonly [string, string, string, string, string] }>> = {
  novice: { step: 0, colors: ['#8a9298', '#4f565b', '#dde2e5', '#eef6fb', '#b9c9d4'] },
  practitioner: { step: 1, colors: ['#c8643b', '#7a3418', '#f6c6a8', '#fbf1ea', '#e4c4b0'] },
  proficient: { step: 2, colors: ['#9a6e34', '#5a3d13', '#e8cc98', '#f8f1e3', '#ddc9a2'] },
  expert: { step: 3, colors: ['#b39a4c', '#5f4f1c', '#ece0b0', '#fbf8ee', '#e2d8b8'] },
  specialist: { step: 4, colors: ['#a7b4c2', '#556373', '#f4f7fa', '#eef5fc', '#c7d6e6'] },
  authority: { step: 5, colors: ['#e3b021', '#8f6a00', '#fff3c4', '#fff9e3', '#f1dd98'] },
  luminary: { step: 6, colors: ['#9fb3c8', '#2a3a48', '#ffffff', '#1d2b33', '#0f1c1f'] },
};

const BEAMS = Array.from({ length: 12 }, (_, k) => {
  const angle = (k * Math.PI) / 6 - Math.PI / 2;
  const at = (radius: number, shift: number) =>
    (32 + radius * Math.cos(angle + shift)).toFixed(1) + ',' + (36 + radius * Math.sin(angle + shift)).toFixed(1);
  return [at(12, -0.05), at(k % 2 ? 41 : 46, 0), at(12, 0.05)].join(' ');
});

const CLAMPS = [45, 135, 225, 315].map((degrees) => {
  const angle = (degrees * Math.PI) / 180;
  return { x: (32 + 30 * Math.cos(angle)).toFixed(1), y: (36 + 30 * Math.sin(angle)).toFixed(1), a: degrees + 90 };
});

/** The metal of a specialty tier: its step, colour, deep and light. */
export function specialtyMetal(tier: string): { step: number; color: string; deep: string; light: string } {
  const { step, colors } = METALS[tier] ?? METALS['novice'];
  return { step, color: colors[0], deep: colors[1], light: colors[2] };
}

let medallions = 0;

/**
 * The emblem of a specialty: a round medallion, like a lens in its mount, in a metal more noble
 * tier after tier, around an instrument of optics, from the reading stone to the prism. Decorative.
 */
@Component({
  selector: 'app-specialty-emblem',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'specialty-emblem' },
  template: `
    @let m = metal();
    @let s = m.step;
    <svg [attr.width]="size()" [attr.height]="size()" viewBox="-6 -2 76 76" aria-hidden="true">
      <defs>
        <linearGradient [attr.id]="id + '-metal'" x1="0" y1="0" x2="0.4" y2="1">
          <stop offset="0" [attr.stop-color]="m.light"></stop>
          <stop offset="0.45" [attr.stop-color]="m.color"></stop>
          <stop offset="1" [attr.stop-color]="m.deep"></stop>
        </linearGradient>
        <linearGradient [attr.id]="id + '-rim'" x1="0" y1="1" x2="0" y2="0">
          <stop offset="0" [attr.stop-color]="m.light"></stop>
          <stop offset="0.5" [attr.stop-color]="m.color"></stop>
          <stop offset="1" [attr.stop-color]="m.deep"></stop>
        </linearGradient>
        <radialGradient [attr.id]="id + '-glass'" cx="0.38" cy="0.3" r="0.8">
          <stop offset="0" stop-color="#ffffff"></stop>
          <stop offset="0.5" [attr.stop-color]="m.glass"></stop>
          <stop offset="1" [attr.stop-color]="m.glassEdge"></stop>
        </radialGradient>
        <radialGradient [attr.id]="id + '-aura'" cx="0.5" cy="0.5" r="0.5">
          <stop offset="0" [attr.stop-color]="m.aura"></stop>
          <stop offset="0.6" [attr.stop-color]="m.aura" stop-opacity="0.5"></stop>
          <stop offset="1" [attr.stop-color]="m.aura" stop-opacity="0"></stop>
        </radialGradient>
        <linearGradient [attr.id]="id + '-spectrum'" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#ff6b6b"></stop>
          <stop offset="0.2" stop-color="#ffa94d"></stop>
          <stop offset="0.4" stop-color="#ffe066"></stop>
          <stop offset="0.6" stop-color="#69db7c"></stop>
          <stop offset="0.8" stop-color="#4dabf7"></stop>
          <stop offset="1" stop-color="#9775fa"></stop>
        </linearGradient>
        <filter [attr.id]="id + '-glow'" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur [attr.stdDeviation]="2 + s * 0.7"></feGaussianBlur></filter>
        <filter [attr.id]="id + '-shadow'" x="-30%" y="-30%" width="160%" height="160%"><feDropShadow dx="0" dy="2.2" stdDeviation="1.6" flood-color="#0f1c1f" flood-opacity="0.32"></feDropShadow></filter>
        <clipPath [attr.id]="id + '-lens'"><circle cx="32" cy="36" r="22.6"></circle></clipPath>
      </defs>
      @if (s >= 1) {
        <circle cx="32" cy="36" [attr.r]="36 + s * 2.4" [attr.fill]="url('aura')" [attr.opacity]="m.auraOpacity" [attr.filter]="url('glow')"></circle>
      }
      @if (s >= 5) {
        @for (beam of beams; track $index) {
          <polygon [attr.points]="beam" [attr.fill]="s === 6 ? '#e7f5ff' : m.light" opacity="0.45"></polygon>
        }
      }
      @if (s === 6) {
        <circle cx="32" cy="36" r="42" fill="none" [attr.stroke]="url('spectrum')" stroke-width="2.6"></circle>
      }
      @if (s >= 3) {
        <circle cx="32" cy="36" r="36.6" fill="none" [attr.stroke]="url('metal')" stroke-width="2.2"></circle>
        <circle cx="32" cy="36" r="34" fill="none" [attr.stroke]="m.deep" stroke-width="3" stroke-dasharray="0.6 2.9" opacity="0.75"></circle>
      }
      <g [attr.filter]="url('shadow')">
        @if (s >= 4) {
          @for (clamp of clamps; track $index) {
            <rect x="-3.6" y="-4.2" width="7.2" height="8.4" rx="1.6" [attr.fill]="url('metal')" [attr.stroke]="m.deep" stroke-width="0.8" [attr.transform]="'translate(' + clamp.x + ' ' + clamp.y + ') rotate(' + clamp.a + ')'"></rect>
            <circle [attr.cx]="clamp.x" [attr.cy]="clamp.y" r="1.3" [attr.fill]="m.deep"></circle>
          }
        }
        <circle cx="32" cy="36" r="30.4" [attr.fill]="m.deep"></circle>
        <circle cx="32" cy="36" r="29" [attr.fill]="url('metal')"></circle>
        @if (s >= 2) {
          <circle cx="32" cy="36" r="29" fill="none" [attr.stroke]="m.deep" stroke-width="1.8" stroke-dasharray="0.7 1.3" opacity="0.55"></circle>
        }
        <path d="M10 28 A23 23 0 0 1 46 13" fill="none" stroke="#ffffff" stroke-width="1.5" stroke-linecap="round" opacity="0.55"></path>
        <circle cx="32" cy="36" r="24.4" [attr.fill]="url('rim')"></circle>
        @if (s >= 1) {
          <circle cx="32" cy="36" r="26.6" fill="none" [attr.stroke]="m.light" stroke-width="0.8" opacity="0.8"></circle>
        }
        <circle cx="32" cy="36" r="22.6" [attr.fill]="url('glass')"></circle>
        <g [attr.clip-path]="url('lens')">
          <g transform="translate(11.5 15.5) scale(0.41)" stroke-linejoin="round" stroke-linecap="round">
            @switch (s) {
              @case (0) {
                <rect x="12" y="28" width="76" height="5" rx="2" [attr.fill]="m.deep" opacity="0.45"></rect>
                <rect x="12" y="40" width="64" height="5" rx="2" [attr.fill]="m.deep" opacity="0.45"></rect>
                <rect x="12" y="76" width="70" height="5" rx="2" [attr.fill]="m.deep" opacity="0.45"></rect>
                <path d="M16 74 A34 30 0 0 1 84 74 Z" fill="#ffffff" fill-opacity="0.55" [attr.stroke]="m.deep" stroke-width="3.5"></path>
                <rect x="24" y="52" width="52" height="8" rx="3" [attr.fill]="m.deep" opacity="0.75"></rect>
                <rect x="24" y="64" width="40" height="8" rx="3" [attr.fill]="m.deep" opacity="0.75"></rect>
                <path d="M28 58 A24 22 0 0 1 48 48" fill="none" stroke="#ffffff" stroke-width="4"></path>
              }
              @case (1) {
                <path d="M30 66 L50 90 L70 66" fill="none" [attr.stroke]="m.color" stroke-width="7"></path>
                <path d="M30 66 L50 90 L70 66" fill="none" [attr.stroke]="m.deep" stroke-width="2"></path>
                <circle cx="50" cy="90" r="5" [attr.fill]="m.deep"></circle>
                <circle cx="29" cy="48" r="19" fill="#ffffff" fill-opacity="0.55" [attr.stroke]="m.deep" stroke-width="7"></circle>
                <circle cx="71" cy="48" r="19" fill="#ffffff" fill-opacity="0.55" [attr.stroke]="m.deep" stroke-width="7"></circle>
                <path d="M20 42 A10 10 0 0 1 28 36 M62 42 A10 10 0 0 1 70 36" fill="none" stroke="#ffffff" stroke-width="4"></path>
              }
              @case (2) {
                <line x1="60" y1="60" x2="88" y2="88" [attr.stroke]="m.deep" stroke-width="13"></line>
                <line x1="60" y1="60" x2="88" y2="88" [attr.stroke]="m.color" stroke-width="7"></line>
                <circle cx="42" cy="42" r="27" fill="#ffffff" fill-opacity="0.5" [attr.stroke]="m.color" stroke-width="8"></circle>
                <circle cx="42" cy="42" r="27" fill="none" [attr.stroke]="m.deep" stroke-width="2.5"></circle>
                <path d="M26 36 A16 16 0 0 1 38 24" fill="none" stroke="#ffffff" stroke-width="5"></path>
              }
              @case (3) {
                <g transform="rotate(-32 50 50)">
                  <rect x="4" y="38" width="30" height="24" rx="3" [attr.fill]="m.color" [attr.stroke]="m.deep" stroke-width="3.5"></rect>
                  <rect x="32" y="41" width="30" height="18" rx="2" [attr.fill]="m.color" [attr.stroke]="m.deep" stroke-width="3.5"></rect>
                  <rect x="60" y="44" width="28" height="12" rx="2" [attr.fill]="m.color" [attr.stroke]="m.deep" stroke-width="3.5"></rect>
                  <rect x="86" y="46" width="8" height="8" rx="1.5" [attr.fill]="m.deep"></rect>
                  <ellipse cx="4" cy="50" rx="4" ry="12" fill="#ffffff" fill-opacity="0.75" [attr.stroke]="m.deep" stroke-width="3"></ellipse>
                  <path d="M10 44 H28 M38 46 H56" stroke="#ffffff" stroke-width="3" opacity="0.6"></path>
                </g>
              }
              @case (4) {
                <ellipse cx="50" cy="90" rx="30" ry="7" [attr.fill]="m.deep"></ellipse>
                <path d="M66 88 V44 Q66 30 54 26" fill="none" [attr.stroke]="m.deep" stroke-width="10"></path>
                <path d="M66 88 V44 Q66 30 54 26" fill="none" [attr.stroke]="m.color" stroke-width="5"></path>
                <rect x="22" y="70" width="40" height="6" rx="2" [attr.fill]="m.deep"></rect>
                <g transform="rotate(-14 42 42)">
                  <rect x="34" y="12" width="16" height="50" rx="3" [attr.fill]="m.color" [attr.stroke]="m.deep" stroke-width="3.5"></rect>
                  <rect x="36" y="4" width="12" height="10" rx="2" [attr.fill]="m.deep"></rect>
                  <rect x="38" y="62" width="8" height="8" rx="1.5" [attr.fill]="m.deep"></rect>
                  <path d="M38 18 V54" stroke="#ffffff" stroke-width="3" opacity="0.6"></path>
                </g>
              }
              @case (5) {
                <path d="M50 66 L28 94 M50 66 L72 94 M50 66 L50 96" [attr.stroke]="m.deep" stroke-width="5"></path>
                <circle cx="50" cy="64" r="7" [attr.fill]="m.deep"></circle>
                <g transform="rotate(-30 50 48)">
                  <rect x="8" y="36" width="80" height="24" rx="4" [attr.fill]="m.color" [attr.stroke]="m.deep" stroke-width="3.5"></rect>
                  <path d="M26 36 V60 M70 36 V60" [attr.stroke]="m.deep" stroke-width="3"></path>
                  <rect x="72" y="26" width="9" height="11" rx="2" [attr.fill]="m.deep"></rect>
                  <ellipse cx="8" cy="48" rx="4" ry="12" [attr.fill]="m.deep"></ellipse>
                  <path d="M30 41 H66" stroke="#ffffff" stroke-width="3" opacity="0.6"></path>
                </g>
              }
              @default {
                <line x1="2" y1="64" x2="44" y2="55" stroke="#ffffff" stroke-width="5"></line>
                <polygon points="62,52 100,30 100,40" fill="#ff6b6b"></polygon>
                <polygon points="62,52 100,40 100,50" fill="#ffa94d"></polygon>
                <polygon points="62,52 100,50 100,60" fill="#ffe066"></polygon>
                <polygon points="62,52 100,60 100,70" fill="#69db7c"></polygon>
                <polygon points="62,52 100,70 100,80" fill="#4dabf7"></polygon>
                <polygon points="62,52 100,80 100,90" fill="#9775fa"></polygon>
                <polygon points="50,14 84,76 16,76" fill="#ffffff" fill-opacity="0.3" stroke="#f2c94c" stroke-width="3.5"></polygon>
                <path d="M50 22 L28 68" stroke="#ffffff" stroke-width="3" opacity="0.7"></path>
              }
            }
          </g>
        </g>
        <path d="M14 30 A19 19 0 0 1 30 17.5" fill="none" stroke="#ffffff" stroke-width="2.4" stroke-linecap="round" opacity="0.7"></path>
        <circle cx="16.5" cy="25" r="1.1" fill="#ffffff" opacity="0.9"></circle>
        <path d="M52 50 A22 22 0 0 1 36 58" fill="none" stroke="#ffffff" stroke-width="1" stroke-linecap="round" opacity="0.35"></path>
      </g>
    </svg>
  `,
})
export class SpecialtyEmblemComponent {
  readonly tier = input.required<string>();
  readonly size = input(48);

  protected readonly id = 'medallion-' + medallions++;
  protected readonly beams = BEAMS;
  protected readonly clamps = CLAMPS;
  protected readonly metal = computed(() => {
    const { step, colors } = METALS[this.tier()] ?? METALS['novice'];
    const [color, deep, light, glass, glassEdge] = colors;
    return {
      step, color, deep, light, glass, glassEdge,
      aura: step === 6 ? '#a5d8ff' : color,
      auraOpacity: [0, 0.3, 0.36, 0.44, 0.52, 0.62, 0.8][step],
    };
  });

  protected url(part: string): string {
    return `url(#${this.id}-${part})`;
  }
}

import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** The metals of the levels, from the darkest to the lightest: shadow, deep, mid, light. */
const RAMPS = {
  locked: ['#8d9aa6', '#b3bfc9', '#d3dbe2', '#eef3f6'],
  bronze: ['#2e1708', '#7a4a20', '#c98a4b', '#f6d9b8'],
  silver: ['#1f262d', '#5f6d79', '#b9c4cd', '#ffffff'],
  gold: ['#2e2100', '#8f6a00', '#e6b422', '#fff6cf'],
  sapphire: ['#061f3d', '#1864ab', '#4dabf7', '#eaf6ff'],
  amethyst: ['#170b3d', '#5f3dc4', '#9775fa', '#f1ebff'],
  emerald: ['#022d20', '#087f5b', '#20c997', '#e6fcf5'],
} as const;

export type Metal = keyof typeof RAMPS;

const BY_LEVEL: readonly Metal[] = ['locked', 'bronze', 'silver', 'gold', 'sapphire', 'amethyst'];

/** The metal of an achievement: grey before its first level, emerald when it has only one, else by level. */
export function metalOf(level: number, levels: number): Metal {
  if (level <= 0) {
    return 'locked';
  }
  return levels === 1 ? 'emerald' : BY_LEVEL[Math.min(level, BY_LEVEL.length - 1)];
}

/** The tables of a gradient map: each grey of the photo becomes the colour at that height of the ramp. */
export function rampTables(metal: Metal): { r: string; g: string; b: string } {
  const channel = (offset: number) =>
    RAMPS[metal].map((hex) => (parseInt(hex.slice(offset, offset + 2), 16) / 255).toFixed(3)).join(' ');
  return { r: channel(1), g: channel(3), b: channel(5) };
}

let next = 0;

/**
 * An achievement as a figure of its own: a real photo, cut out and grey, tinted with the metal of
 * its level by a gradient map, without a frame. The last level glows.
 */
@Component({
  selector: 'app-achievement-figure',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'achievement-figure',
    '[class.locked]': "metal() === 'locked'",
    '[class.complete]': 'complete()',
    '[style.--glow]': 'glow()',
  },
  template: `
    <svg width="0" height="0" aria-hidden="true" focusable="false">
      <filter [attr.id]="filterId" color-interpolation-filters="sRGB">
        <feColorMatrix type="saturate" values="0" />
        <feComponentTransfer>
          <feFuncR type="table" [attr.tableValues]="tables().r" />
          <feFuncG type="table" [attr.tableValues]="tables().g" />
          <feFuncB type="table" [attr.tableValues]="tables().b" />
        </feComponentTransfer>
      </filter>
    </svg>
    <img [src]="'achievements/' + id() + '.webp'" alt="" [style.filter]="'url(#' + filterId + ')'" />
  `,
})
export class AchievementFigureComponent {
  readonly id = input.required<string>();
  readonly level = input.required<number>();
  /** How many levels the achievement has. */
  readonly levels = input.required<number>();

  protected readonly filterId = `achievement-metal-${next++}`;
  protected readonly metal = computed(() => metalOf(this.level(), this.levels()));
  protected readonly tables = computed(() => rampTables(this.metal()));
  protected readonly complete = computed(() => this.level() > 0 && this.level() >= this.levels());
  protected readonly glow = computed(() => RAMPS[this.metal()][2]);
}

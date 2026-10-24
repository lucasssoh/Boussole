import { emblemLook, emblemStep } from './emblem';
import { specialtyMetal } from './specialty-emblem.component';

/**
 * The frame of a rank pill, one per tier: the metal of its emblem around a body that fills like a
 * gauge, a little richer each tier (a glow, rivets, a gem on top, a second ring), and from Master
 * on, the night of its emblem inside. Given as CSS custom properties, plus the ornaments to draw.
 */
export interface PillFrame {
  vars: Record<string, string>;
  studs: { left: string; top: boolean }[];
  gem: boolean;
  dark: boolean;
  breathing: boolean;
}

interface Shape {
  width: number;
  glow: number;
  studs: number;
  gem: boolean;
  ring: boolean;
  dark: boolean;
  stars: boolean;
  gold: boolean;
}

const shape = (width: number, glow: number, studs: number, gem: boolean, ring: boolean, dark = false, stars = false, gold = false): Shape => ({
  width, glow, studs, gem, ring, dark, stars, gold,
});

/** Curious to Grand Sage. */
const CULTURE: readonly Shape[] = [
  shape(1.5, 0, 0, false, false),
  shape(2, 0.28, 0, false, false),
  shape(2, 0.32, 4, false, false),
  shape(2.5, 0.38, 4, true, false),
  shape(2.5, 0.45, 4, true, true),
  shape(3, 0.55, 4, true, false, true, false, true),
  shape(3, 0.62, 4, true, false, true, true, true),
  shape(3, 0.75, 6, true, true, true, true, true),
];

/** Novice to Luminary: iron to gold, then crystal on a night sky with the spectrum for a frame. */
const SPECIALTY: readonly Shape[] = [
  shape(1.5, 0, 0, false, false),
  shape(2, 0.25, 0, false, false),
  shape(2, 0.3, 4, false, false),
  shape(2.5, 0.36, 4, false, true),
  shape(2.5, 0.44, 4, true, true),
  shape(3, 0.55, 4, true, true),
  shape(3, 0.7, 6, true, true, true, true),
];

const GOLD = 'linear-gradient(135deg, #fff3c4, #e6b422 45%, #8f6a00)';
const SPECTRUM = 'linear-gradient(90deg, #ff6b6b, #ffa94d, #ffe066, #69db7c, #4dabf7, #9775fa)';
const STARS = [[6, 30], [14, 72], [23, 40], [31, 78], [44, 22], [57, 70], [66, 30], [78, 66], [88, 34], [95, 74]];

export function pillFrame(ladder: 'culture' | 'specialty', tier: string): PillFrame {
  const culture = ladder === 'culture';
  const step = culture ? emblemStep(tier) : specialtyMetal(tier).step;
  const top = culture ? step === 7 : step === 6;
  const look = culture ? emblemLook(tier) : top ? { color: '#4dabf7', deep: '#1d2b33', light: '#e7f5ff' } : specialtyMetal(tier);
  const { color, deep, light } = look;
  const s = (culture ? CULTURE : SPECIALTY)[step];
  const frame = s.gold ? GOLD : top && !culture ? SPECTRUM : `linear-gradient(135deg, ${light}, ${color} 45%, ${deep})`;
  const accent = s.gold ? '#e6b422' : color;
  const half = s.studs / 2;
  return {
    vars: {
      '--frame': frame,
      '--frame-width': `${s.width}px`,
      '--fill': s.dark ? (top ? '#3d5560' : color) : blend(light, color, 0.3),
      '--rest': s.dark ? blend(deep, '#000000', 0.35) : '#ffffff',
      '--edge': s.gold ? '#f2c94c' : s.dark ? light : color,
      '--stars': s.stars ? stars(s.gold ? '#fff3c4' : light) : 'none',
      '--ring': s.ring ? `0 0 0 2px var(--surface), 0 0 0 3.5px ${accent}` : '0 0 0 transparent',
      '--press': blend(deep, '#ffffff', 0.55),
      '--aura': s.gold && top ? '#f2c94c' : color,
      '--glow': String(s.glow),
      '--stud': s.gold ? '#f2c94c' : light,
      '--stud-ring': deep,
      '--gem': s.gold ? color : light,
      '--gem-ring': s.gold ? '#e6b422' : deep,
      '--ink': s.dark ? (s.gold ? '#fff3c4' : '#ffffff') : 'var(--text)',
      '--sub': s.dark ? blend(light, '#ffffff', 0.3) : 'var(--muted)',
    },
    studs: Array.from({ length: s.studs }, (_, k) => ({ left: `${((100 * ((k % half) + 1)) / (half + 1)).toFixed(1)}%`, top: k < half })),
    gem: s.gem,
    dark: s.dark,
    breathing: s.glow >= 0.55,
  };
}

function stars(tint: string): string {
  return STARS.map(([x, y], k) => {
    const r = k % 3 ? 1 : 1.5;
    return `radial-gradient(${r}px ${r}px at ${x}% ${y}%, ${tint}, transparent)`;
  }).join(', ');
}

function blend(a: string, b: string, t: number): string {
  const parts = (hex: string) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));
  const [x, y] = [parts(a), parts(b)];
  return '#' + x.map((v, i) => Math.round(v + (y[i] - v) * t).toString(16).padStart(2, '0')).join('');
}

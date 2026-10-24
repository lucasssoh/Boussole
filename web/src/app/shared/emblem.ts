/**
 * What the emblem of a tier is made of: a hexagonal gem around an instrument of navigation, from
 * the floating needle to the armillary sphere, ornaments added tier after tier (a trim, rivets,
 * laurel, a graduated ring, an oil lamp, rays, a halo), an aura growing with them, and the division
 * as a roman numeral in relief over the lowest vertex. The geometry lives in a 64 by 72 box, the
 * gem centred at (32, 36).
 */

/** The look of the eight steps of the culture ladder: colour, deep, light, then the dial and its edge. */
const PALETTE: readonly (readonly [string, string, string, string, string])[] = [
  ['#7c8f88', '#4d5e58', '#c9d3cf', '#d0ebff', '#a5c8e4'],
  ['#b0703a', '#6e4019', '#f0c9a3', '#fbeee0', '#e0c3a3'],
  ['#6f8fa6', '#3d566b', '#d3e0ea', '#eef3f7', '#c4d3df'],
  ['#d4a017', '#7f5e00', '#fff1bf', '#fffaea', '#ecd9a0'],
  ['#1f6fd1', '#103f7d', '#cfe3fb', '#f8f1e1', '#e3d6b8'],
  ['#7b3fc4', '#45207a', '#e5d6f7', '#f6f0fd', '#d8c6ee'],
  ['#c2255c', '#7a1236', '#fbd3e1', '#fff1bf', '#ecd29a'],
  ['#2a3a42', '#0f1c1f', '#55646b', '#1d2b33', '#0f1c1f'],
];

/** Each tier of the culture on those eight steps. The specialties have no emblems yet. */
const STEP: Readonly<Record<string, number>> = {
  curious: 0, apprentice: 1, initiate: 2, scholar: 3, savant: 4, master: 5, sage: 6, 'grand-sage': 7,
};

/** From morning mist to the night sky: the hour of each step, behind a large emblem. */
const AMBIANCE: readonly string[] = [
  'linear-gradient(135deg, #f6f8f7, #e1e9e6)',
  'linear-gradient(135deg, #fdf6ee, #f0d9c0)',
  'linear-gradient(135deg, #f1f6fa, #d3e1eb)',
  'linear-gradient(135deg, #fffaea, #f3dc95)',
  'linear-gradient(135deg, #e9f1fc, #a9c7ee)',
  'linear-gradient(135deg, #3b2468, #1d1038)',
  stars('#fbd3e1') + 'linear-gradient(135deg, #4b1029, #1a0510)',
  stars('#fff3c4') + 'radial-gradient(circle at 50% 50%, #1b2c33, #05090b 70%)',
];

export interface EmblemLook {
  step: number;
  color: string;
  deep: string;
  mid: string;
  light: string;
  disc: string;
  discEdge: string;
  ornament: string;
  ornamentDeep: string;
  leafLight: string;
  leafDeep: string;
  trimColor: string;
  auraColor: string;
  auraLight: string;
  auraOpacity: number;
  auraLow: number;
  auraHigh: number;
  auraR: number;
  auraRy: number;
  aura2R: number;
  aura2Ry: number;
  blur: number;
  flow: string;
}

export function emblemStep(tier: string): number {
  return STEP[tier] ?? 0;
}

export function emblemLook(tier: string): EmblemLook {
  const step = emblemStep(tier);
  const [color, deep, light, disc, discEdge] = PALETTE[step];
  const gold = step >= 5;
  const top = step === 7;
  return {
    step, color, deep, light, disc, discEdge, mid: mix(color, deep),
    ornament: gold ? '#e6b422' : light, ornamentDeep: gold ? '#8f6a00' : deep,
    leafLight: gold ? '#fff3c4' : '#b2f2bb', leafDeep: gold ? '#b8860b' : '#2b8a3e',
    trimColor: top ? '#f2c94c' : light,
    auraColor: top ? '#f2c94c' : color, auraLight: top ? '#fff3c4' : light,
    auraOpacity: [0, 0.3, 0.38, 0.46, 0.55, 0.65, 0.75, 0.85][step],
    auraLow: top ? 0.7 : 0.55, auraHigh: top ? 1 : 0.9,
    auraR: 33 + step * 2.6, auraRy: 35 + step * 2.8, aura2R: 22 + step * 1.6, aura2Ry: 15 + step * 1.2,
    blur: 2 + step * 0.7,
    flow: ['', '', '', '', '7s', '6s', '5.2s', '4.6s'][step],
  };
}

export function emblemAmbiance(tier: string): string {
  return AMBIANCE[emblemStep(tier)];
}

const CX = 32;
const CY = 36;

function vertex(radius: number, k: number): readonly [number, number] {
  const angle = -Math.PI / 2 + (k * Math.PI) / 3;
  return [CX + radius * Math.cos(angle), CY + radius * Math.sin(angle)];
}

export const HEX = 'M' + [0, 1, 2, 3, 4, 5].map((k) => vertex(32, k).map((v) => v.toFixed(2)).join(' ')).join(' L') + ' Z';

export const RIVETS = [0, 1, 2, 3, 4, 5].map((k) => {
  const [x, y] = vertex(24.5, k);
  return { x: x.toFixed(1), y: y.toFixed(1) };
});

export const RAYS = Array.from({ length: 16 }, (_, k) => {
  const angle = (k * Math.PI) / 8 - Math.PI / 2;
  const at = (radius: number, shift: number) =>
    (CX + radius * Math.cos(angle + shift)).toFixed(1) + ',' + (CY + radius * Math.sin(angle + shift)).toFixed(1);
  return [at(10, -0.12), at(k % 2 ? 40 : 47, 0), at(10, 0.12)].join(' ');
});

/** A laurel branch on each side, rising from under the gem: its stem and its leaves, alternating. */
function branch(side: number) {
  const p0 = [CX - side * 4, 74], c = [CX - side * 40, 70], p1 = [CX - side * 35, 22];
  const point = (t: number) => [
    (1 - t) * (1 - t) * p0[0] + 2 * (1 - t) * t * c[0] + t * t * p1[0],
    (1 - t) * (1 - t) * p0[1] + 2 * (1 - t) * t * c[1] + t * t * p1[1],
  ];
  const along = (t: number) => {
    const dx = 2 * (1 - t) * (c[0] - p0[0]) + 2 * t * (p1[0] - c[0]);
    const dy = 2 * (1 - t) * (c[1] - p0[1]) + 2 * t * (p1[1] - c[1]);
    return (Math.atan2(dy, dx) * 180) / Math.PI + 90;
  };
  const leaves = [];
  for (let k = 0; k < 7; k++) {
    const t = 0.12 + k * 0.13;
    const [x, y] = point(t);
    leaves.push({ x: x.toFixed(1), y: y.toFixed(1), a: (along(t) + (k % 2 === 0 ? -side : side) * 38).toFixed(0) });
  }
  const [x, y] = point(1);
  leaves.push({ x: x.toFixed(1), y: y.toFixed(1), a: along(1).toFixed(0) });
  return { stem: 'M' + p0.join(' ') + ' Q' + c.join(' ') + ' ' + p1.join(' '), leaves };
}

const LEFT = branch(1);
const RIGHT = branch(-1);
export const STEMS = LEFT.stem + ' ' + RIGHT.stem;
export const LEAVES = LEFT.leaves.concat(RIGHT.leaves);

/** Specks of light rising around the two highest steps: where, how big, how long, when. */
export const MOTES = [
  [-6, 58, 0.9, '3.6s', '0s'], [70, 54, 0.7, '4.2s', '-1.1s'], [2, 30, 0.6, '3.9s', '-2.3s'],
  [64, 24, 0.9, '4.6s', '-0.6s'], [-12, 44, 0.6, '5s', '-1.8s'], [76, 40, 0.7, '3.4s', '-2.9s'],
].map(([x, y, r, duration, delay]) => ({ x, y, r, duration, delay }));

/** I and V as shapes with serifs, 13 high: a numeral drawn, so that it can stand in relief. */
const GLYPHS: Readonly<Record<string, { width: number; points: readonly (readonly [number, number])[] }>> = {
  I: { width: 6.4, points: [[-3.2, 0], [3.2, 0], [3.2, 1.8], [1.5, 1.8], [1.5, 11.2], [3.2, 11.2], [3.2, 13], [-3.2, 13], [-3.2, 11.2], [-1.5, 11.2], [-1.5, 1.8], [-3.2, 1.8]] },
  V: { width: 10.6, points: [[-5.3, 0], [-1.7, 0], [0, 7.6], [1.7, 0], [5.3, 0], [1.4, 13], [-1.4, 13]] },
};

/** The path of a roman numeral, centred under the gem, its top at y = 61, over the lowest vertex. */
export function numeralPath(numeral: string): string {
  const gap = 0.9;
  const letters = numeral.split('');
  const total = letters.reduce((sum, letter) => sum + GLYPHS[letter].width, 0) + gap * (letters.length - 1);
  let x = CX - total / 2;
  return letters
    .map((letter) => {
      const glyph = GLYPHS[letter];
      const centre = x + glyph.width / 2;
      x += glyph.width + gap;
      return 'M' + glyph.points.map(([px, py]) => (centre + px).toFixed(2) + ' ' + (61 + py).toFixed(2)).join(' L') + ' Z';
    })
    .join(' ');
}

function mix(a: string, b: string): string {
  const channels = (hex: string) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));
  const [x, y] = [channels(a), channels(b)];
  return '#' + x.map((v, i) => Math.round((v + y[i]) / 2).toString(16).padStart(2, '0')).join('');
}

function stars(tint: string): string {
  const places = [[8, 22], [17, 70], [29, 35], [41, 80], [52, 18], [63, 62], [74, 30], [86, 74], [93, 26], [36, 12]];
  return places
    .map(([x, y], k) => {
      const size = k % 3 ? 1 : 1.6;
      return `radial-gradient(${size}px ${size}px at ${x}% ${y}%, ${tint}, transparent), `;
    })
    .join('');
}

/** The ink that reads on the ambiance of a step: dark by day, light from the dusk of Master. */
export function emblemInk(tier: string): string {
  return emblemStep(tier) >= 5 ? '#f1ece4' : '#3d4f49';
}

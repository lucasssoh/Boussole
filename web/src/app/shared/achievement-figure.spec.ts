import { describe, expect, it } from 'vitest';
import { metalOf, rampTables } from './achievement-figure.component';

describe('achievement figure', () => {
  it('turns the metal with the level, grey before the first', () => {
    expect([0, 1, 2, 3, 4, 5].map((level) => metalOf(level, 5))).toEqual(['locked', 'bronze', 'silver', 'gold', 'sapphire', 'amethyst']);
  });

  it('gives an achievement of a single level its own emerald, without a level', () => {
    expect(metalOf(1, 1)).toBe('emerald');
    expect(metalOf(0, 1)).toBe('locked');
  });

  it('maps the grey of a photo onto the ramp of its metal, from dark to light', () => {
    const silver = rampTables('silver');
    expect(silver.r.split(' ')).toHaveLength(4);
    expect(silver.r.endsWith('1.000')).toBe(true);
    expect(Number(silver.b.split(' ')[0])).toBeLessThan(Number(silver.b.split(' ')[3]));
  });
});

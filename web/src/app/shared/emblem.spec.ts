import { describe, expect, it } from 'vitest';
import { emblemLook, emblemStep, numeralPath } from './emblem';

describe('emblem', () => {
  it('puts the tiers of the culture on eight steps', () => {
    expect(emblemStep('curious')).toBe(0);
    expect(emblemStep('grand-sage')).toBe(7);
    expect(emblemStep('unknown')).toBe(0);
  });

  it('gives the aura to every tier but the first, stronger as they rise', () => {
    const opacities = ['curious', 'apprentice', 'scholar', 'sage', 'grand-sage'].map((tier) => emblemLook(tier).auraOpacity);
    expect(opacities[0]).toBe(0);
    expect([...opacities].sort((a, b) => a - b)).toEqual(opacities);
  });

  it('draws one shape per letter of the numeral, centred under the gem', () => {
    expect(numeralPath('III').match(/M/g)).toHaveLength(3);
    const xs = numeralPath('IV').match(/[\d.]+(?= [\d.]+)/g)!.map(Number);
    expect((Math.min(...xs) + Math.max(...xs)) / 2).toBeCloseTo(32, 1);
  });
});

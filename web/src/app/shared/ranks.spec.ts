import { describe, expect, it } from 'vitest';
import { divisionProgress, formatPoints, isHeld, levelGauge, rankOrder, roman } from './ranks';

describe('ranks', () => {
  it('orders ranks by tier, then by division, I above V', () => {
    expect(rankOrder({ tier: 'apprentice', division: 4 })).toBeGreaterThan(rankOrder({ tier: 'apprentice', division: 5 }));
    expect(rankOrder({ tier: 'initiate', division: 5 })).toBeGreaterThan(rankOrder({ tier: 'apprentice', division: 1 }));
    expect(rankOrder({ tier: 'grand-sage', division: 0 })).toBeGreaterThan(rankOrder({ tier: 'sage', division: 1 }));
  });

  it('writes divisions in roman numerals, none at the top', () => {
    expect(roman(3)).toBe('III');
    expect(roman(0)).toBe('');
  });

  it('measures the current level and the points left before the next', () => {
    expect(levelGauge(700, 40, 730)).toEqual({ share: 0.75, left: 10 });
    expect(levelGauge(700, 40, 729.5)).toEqual({ share: 0.7375, left: 11 });
    expect(levelGauge(700, 40, 760)).toEqual({ share: 1, left: 0 });
  });

  it('knows a rank that waits for fields', () => {
    const rank = { tier: 'savant', division: 1, level: 70, intoLevel: 0, levelSize: 1, divisionFrom: 0, nextAt: 1, specialtiesMissing: 0, criticalThinkingMissing: false };
    expect(isHeld(rank)).toBe(false);
    expect(isHeld({ ...rank, specialtiesMissing: 2 })).toBe(true);
    expect(isHeld({ ...rank, criticalThinkingMissing: true })).toBe(true);
  });

  it('measures how far into its division a rank is', () => {
    const rank = { tier: 'curious', division: 5, level: 1, intoLevel: 0, levelSize: 1, divisionFrom: 100, nextAt: 200, specialtiesMissing: 0, criticalThinkingMissing: false };
    expect(divisionProgress(rank, 150)).toBe(0.5);
    expect(divisionProgress(rank, 250)).toBe(1);
  });

  it('shows whole points whole, and the others with one decimal', () => {
    expect(formatPoints(12, 'en')).toBe('12');
    expect(formatPoints(1.5, 'fr')).toBe('1,5');
    expect(formatPoints(1 / 3, 'en')).toBe('0.3');
  });
});

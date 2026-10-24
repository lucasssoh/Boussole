import { describe, expect, it } from 'vitest';
import { divisionProgress, formatPoints, rankOrder, roman } from './ranks';

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

  it('measures how far into its division a rank is', () => {
    const rank = { tier: 'curious', division: 5, level: 1, intoLevel: 0, levelSize: 1, divisionFrom: 100, nextAt: 200, fieldsMissing: 0 };
    expect(divisionProgress(rank, 150)).toBe(0.5);
    expect(divisionProgress(rank, 250)).toBe(1);
  });

  it('shows whole points whole, and the others with one decimal', () => {
    expect(formatPoints(12, 'en')).toBe('12');
    expect(formatPoints(1.5, 'fr')).toBe('1,5');
    expect(formatPoints(1 / 3, 'en')).toBe('0.3');
  });
});

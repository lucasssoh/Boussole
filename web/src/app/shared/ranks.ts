import { Rank } from '../core/api';

/** The colour of each tier of the culture ladder, and of the specialty ladder: [fill, deep]. */
const TIER_COLORS: Readonly<Record<string, readonly [string, string]>> = {
  curious: ['#7c8f88', '#4d5e58'],
  apprentice: ['#b0703a', '#7a4a20'],
  initiate: ['#6f8fa6', '#45627a'],
  scholar: ['#d4a017', '#8f6a00'],
  savant: ['#1f6fd1', '#124a91'],
  master: ['#7b3fc4', '#4f2486'],
  sage: ['#c2255c', '#86143c'],
  'grand-sage': ['#1d2b33', '#f2c94c'],
  novice: ['#7c8f88', '#4d5e58'],
  practitioner: ['#b0703a', '#7a4a20'],
  proficient: ['#6f8fa6', '#45627a'],
  expert: ['#d4a017', '#8f6a00'],
  specialist: ['#1f6fd1', '#124a91'],
  authority: ['#7b3fc4', '#4f2486'],
  luminary: ['#1d2b33', '#f2c94c'],
};

export function tierColors(tier: string): readonly [string, string] {
  return TIER_COLORS[tier] ?? TIER_COLORS['curious'];
}

/** The tiers of the culture ladder, from the lowest. */
export const CULTURE_TIERS: readonly string[] = ['curious', 'apprentice', 'initiate', 'scholar', 'savant', 'master', 'sage', 'grand-sage'];

/** The tiers of the specialty ladder, from the lowest. */
export const SPECIALTY_TIERS: readonly string[] = ['novice', 'practitioner', 'proficient', 'expert', 'specialist', 'authority', 'luminary'];

/** The specialty tier that counts for Sage and Grand Sage, and the field Grand Sage asks for besides. */
export const STRONG_SPECIALTY = 'specialist';
export const CRITICAL_THINKING = 'critical-thinking';

/** Whether a specialty is at Specialist or above. */
export function isStrong(tier: string): boolean {
  return SPECIALTY_TIERS.indexOf(tier) >= SPECIALTY_TIERS.indexOf(STRONG_SPECIALTY);
}

/** Where a culture rank stands, higher being better: the tier first, then the division, I above V. */
export function rankOrder(rank: { tier: string; division: number }): number {
  return ladderOrder(CULTURE_TIERS, rank);
}

/** Where a rank stands on its ladder, the culture or a specialty, higher being better. */
export function ladderOrder(tiers: readonly string[], rank: { tier: string; division: number }): number {
  return tiers.indexOf(rank.tier) * 10 + (rank.division === 0 ? 9 : 6 - rank.division);
}

const ROMAN = ['', 'I', 'II', 'III', 'IV', 'V'];

/** A division in roman numerals, V the lowest; the top tier has none. */
export function roman(division: number): string {
  return ROMAN[division] ?? '';
}

/** How far into its division a rank is, from 0 to 1. */
export function divisionProgress(rank: Rank, points: number): number {
  const span = rank.nextAt - rank.divisionFrom;
  return span <= 0 ? 0 : Math.min(1, Math.max(0, (points - rank.divisionFrom) / span));
}

/** Points as shown: whole when they are, else with one decimal, in the language of the interface. */
export function formatPoints(points: number, language: string): string {
  const rounded = Math.round(points * 10) / 10;
  return rounded.toLocaleString(language, { maximumFractionDigits: 1 });
}

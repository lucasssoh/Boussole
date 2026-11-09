import { RubricCriterion } from '../../core/api';

/** The share of the rubric's weight that the criteria ticked add up to, from 0 to 1. */
export function rubricShare(criteria: readonly RubricCriterion[], met: ReadonlySet<number>): number {
  const total = criteria.reduce((sum, criterion) => sum + criterion.weight, 0);
  const ticked = criteria.reduce((sum, criterion, index) => sum + (met.has(index) ? criterion.weight : 0), 0);
  return total > 0 ? ticked / total : 0;
}

/** A weight as the language writes it, and whether it takes the plural: 1 point, 1,5 point, 2 points; 1 point, 1.5 points. */
export function weightWords(weight: number, lang: string): { weight: string; plural: boolean } {
  const french = lang.startsWith('fr');
  return {
    weight: weight.toLocaleString(lang, { maximumFractionDigits: 2 }),
    plural: french ? weight >= 2 : weight !== 1,
  };
}

/** The criteria ticked, with one more or one less. */
export function toggled(met: ReadonlySet<number>, index: number): Set<number> {
  const next = new Set(met);
  if (next.has(index)) {
    next.delete(index);
  } else {
    next.add(index);
  }
  return next;
}

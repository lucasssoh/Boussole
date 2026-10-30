import { BlockView } from '../core/api';

/** The stars of an answer are counted out of 3. */
export const MAX_STARS = 3;

/**
 * How a question shows its stars: gold when they count, blue for a question already met in another
 * session (a warmup, a review: they add nothing to the total), outlined for a free exercise, which
 * the learner grades themselves. A hook is a guess before the lesson: no stars.
 */
export type StarTone = 'gold' | 'blue' | 'outline';

export function starTone(question: { hook: boolean; reused: boolean; free: boolean }): StarTone | null {
  if (question.hook) {
    return null;
  }
  if (question.free) {
    return 'outline';
  }
  return question.reused ? 'blue' : 'gold';
}

/** The tone of a question block, or null for a block that is not a question or a hook. */
export function blockStarTone(block: BlockView): StarTone | null {
  if (block.type !== 'mcq' && block.type !== 'exercise') {
    return null;
  }
  return starTone({ hook: block.hook, reused: block.reused, free: block.type === 'exercise' && block.exerciseType === 'free' });
}

export interface StarredQuestion {
  id: string;
  tone: StarTone | null;
}

/**
 * The stars of some questions: those earned by gold ones, out of 3 for each gold question,
 * answered or not; and the stars of each answer, gold and blue apart.
 */
export function starSummary(questions: StarredQuestion[], stars: ReadonlyMap<string, number>): {
  stars: number;
  possible: number;
  gold: number[];
  blue: number[];
} {
  const summary = { stars: 0, possible: 0, gold: [] as number[], blue: [] as number[] };
  const seen = new Set<string>();
  for (const question of questions) {
    if (seen.has(question.id) || question.tone === null || question.tone === 'outline') {
      continue;
    }
    seen.add(question.id);
    const earned = stars.get(question.id);
    if (question.tone === 'gold') {
      summary.possible += MAX_STARS;
      summary.stars += earned ?? 0;
    }
    if (earned !== undefined) {
      summary[question.tone].push(earned);
    }
  }
  return summary;
}

/** The stars of several answers taken together: their mean, rounded. */
export function averageStars(stars: number[]): number {
  return stars.length === 0 ? 0 : Math.round(stars.reduce((sum, each) => sum + each, 0) / stars.length);
}

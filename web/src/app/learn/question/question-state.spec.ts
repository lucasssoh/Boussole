import { AttemptResult, BlockView, QuestionProgress, RevealedSolution } from '../../core/api';
import { BoussoleApi } from '../../core/boussole-api';
import { storeValue } from '../../core/local-preferences';
import { QuestionState } from './question-state';

const solution = (markdown: string): RevealedSolution => ({
  blocks: [{ type: 'prose', markdown } as BlockView],
  expected: {} as RevealedSolution['expected'],
});
const progress: QuestionProgress = {
  attempts: 1,
  solved: false,
  lastVerdict: 'partly',
  answeredHere: 'partly',
  answeredStars: 2,
};

function keep(written: RevealedSolution): void {
  const result = { verdict: 'partly', items: [], progress, solution: written, stars: 2 } as unknown as AttemptResult;
  storeValue('answer.oc.s21.s21-a4', JSON.stringify({ request: { kind: 'gaps', values: { 1: '4' } }, result }));
}

function question(api: Partial<BoussoleApi>): QuestionState {
  return new QuestionState(api as BoussoleApi, 'oc', 's21', 's21-a4', progress, { total: 2, revealed: [] }, null);
}

const settled = () => new Promise((resolve) => setTimeout(resolve));

describe('QuestionState, an answer given in an earlier sitting', () => {
  afterEach(() => localStorage.clear());

  it('shows the answer this browser kept, with the solution as the track now writes it', async () => {
    keep(solution('2/3 + 10/3 = 4'));
    const state = question({ solution: async () => solution('Formule : x_I = (A^I)^{-1} a') });
    await settled();
    expect(state.gaps()).toEqual({ 1: '4' });
    expect(state.solution()?.blocks).toEqual([{ type: 'prose', markdown: 'Formule : x_I = (A^I)^{-1} a' }]);
  });

  it('falls back on the solution kept when the server cannot give it', async () => {
    keep(solution('2/3 + 10/3 = 4'));
    const state = question({ solution: async () => { throw new Error('offline'); } });
    await settled();
    expect(state.solution()?.blocks).toEqual([{ type: 'prose', markdown: '2/3 + 10/3 = 4' }]);
    expect(state.error()).toBeNull();
  });
});

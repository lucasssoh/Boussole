import { rubricShare, toggled, weightWords } from './rubric';

describe('rubric', () => {
  const criteria = [
    { text: 'La fabrique est abstraite.', weight: 2 },
    { text: 'Cadre ne nomme aucune classe concrète.', weight: 1 },
    { text: 'Le main choisit la fabrique.', weight: 1 },
  ];

  it('adds up the weight of the criteria ticked', () => {
    expect(rubricShare(criteria, new Set())).toBe(0);
    expect(rubricShare(criteria, new Set([0, 2]))).toBe(0.75);
    expect(rubricShare(criteria, new Set([0, 1, 2]))).toBe(1);
    expect(rubricShare([], new Set([0]))).toBe(0);
  });

  it('writes a weight as each language does', () => {
    expect(weightWords(1.5, 'fr')).toEqual({ weight: '1,5', plural: false });
    expect(weightWords(2, 'fr')).toEqual({ weight: '2', plural: true });
    expect(weightWords(1, 'en')).toEqual({ weight: '1', plural: false });
    expect(weightWords(0.5, 'en')).toEqual({ weight: '0.5', plural: true });
  });

  it('ticks and unticks a criterion', () => {
    expect([...toggled(new Set([1]), 2)].sort()).toEqual([1, 2]);
    expect([...toggled(new Set([1, 2]), 1)]).toEqual([2]);
  });
});

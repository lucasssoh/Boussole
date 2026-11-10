import { KEYWORD_CLOSE as C, KEYWORD_OPEN as O, markKeywords, splitComment } from './algorithm-lines';

describe('markKeywords', () => {
  it('marks whole keywords, French or English, whatever their case', () => {
    expect(markKeywords('Pour chaque tâche faire')).toBe(`${O}Pour chaque${C} tâche ${O}faire${C}`);
    expect(markKeywords('while x > 0 do')).toBe(`${O}while${C} x > 0 ${O}do${C}`);
    expect(markKeywords("répéter jusqu'à ce que tout soit placé")).toBe(`${O}répéter${C} ${O}jusqu'à${C} ce que tout soit placé`);
  });

  it('leaves words that only contain a keyword, and math, alone', () => {
    expect(markKeywords('simplifier le format')).toBe('simplifier le format');
    expect(markKeywords('si $x \\text{ si } y$ alors')).toBe(`${O}si${C} $x \\text{ si } y$ ${O}alors${C}`);
  });
});

describe('splitComment', () => {
  it('cuts the comment after // or ▷, outside math', () => {
    expect(splitComment('placer $J_j$   // la première')).toEqual({ step: 'placer $J_j$', comment: 'la première' });
    expect(splitComment('$a // b$ puis c ▷ fin de boucle')).toEqual({ step: '$a // b$ puis c', comment: 'fin de boucle' });
    expect(splitComment('retourner $C$')).toEqual({ step: 'retourner $C$', comment: null });
  });
});

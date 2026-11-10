import { KEYWORD_CLOSE as C, KEYWORD_OPEN as O, markKeywords, splitComment } from './algorithm-lines';

describe('markKeywords', () => {
  it('marks whole keywords, French or English, whatever their case', () => {
    expect(markKeywords('Pour chaque tâche faire')).toBe(`${O}Pour chaque${C} tâche ${O}faire${C}`);
    expect(markKeywords('while x > 0 do')).toBe(`${O}while${C} x > 0 ${O}do${C}`);
    expect(markKeywords("  jusqu'à ce que tout soit placé")).toBe(`  ${O}jusqu'à${C} ce que tout soit placé`);
  });

  it('marks a keyword only where it opens or closes the line', () => {
    expect(markKeywords('pour tout $i \\in F$ faire')).toBe(`${O}pour tout${C} $i \\in F$ ${O}faire${C}`);
    expect(markKeywords('calculer la pénalité pour tout $i \\in F$')).toBe('calculer la pénalité pour tout $i \\in F$');
    expect(markKeywords('placer la tâche si possible, sinon la garder')).toBe('placer la tâche si possible, sinon la garder');
    expect(markKeywords('retourner $C$')).toBe(`${O}retourner${C} $C$`);
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

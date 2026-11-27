import { BlockView } from '../core/api';
import { landmarkKey, pinnablePieces } from './landmarks';

describe('pinnablePieces', () => {
  it('finds the formulas set apart and the tables, in reading order, and skips code', () => {
    const markdown = [
      'Exemple du cours :',
      '',
      '$$',
      '\\begin{cases} \\max & z = -x_1 + x_2 \\end{cases}',
      '$$',
      '',
      '```',
      '$$ pas une formule $$',
      '```',
      '',
      '| Base | z |',
      '|---|---|',
      '| {3, 4, 5} | 0 |',
      '',
      'Puis $$x = 1$$ sur une ligne.',
      '$$x_I = (A^I)^{-1}a$$',
    ].join('\n');

    expect(pinnablePieces(markdown)).toEqual([
      { kind: 'math', markdown: '$$\n\\begin{cases} \\max & z = -x_1 + x_2 \\end{cases}\n$$' },
      { kind: 'table', markdown: '| Base | z |\n|---|---|\n| {3, 4, 5} | 0 |' },
      { kind: 'math', markdown: '$$x_I = (A^I)^{-1}a$$' },
    ]);
  });
});

describe('landmarkKey', () => {
  it('is the same for the same content, and differs for another', () => {
    const program: BlockView[] = [{ type: 'prose', markdown: '$$\\max z$$' }];
    expect(landmarkKey(program)).toBe(landmarkKey([{ type: 'prose', markdown: '$$\\max z$$' }]));
    expect(landmarkKey(program)).not.toBe(landmarkKey([{ type: 'prose', markdown: '$$\\min z$$' }]));
  });
});

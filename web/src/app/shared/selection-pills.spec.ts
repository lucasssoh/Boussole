import { clipBox, pillOf, selectionLines } from './selection-pills';

describe('selection pills', () => {
  it('joins the words of one line, a word in bold or a formula included, into one line', () => {
    const lines = selectionLines([
      { left: 120, right: 180, top: 10, bottom: 33 },
      { left: 10, right: 118, top: 10, bottom: 33 },
      { left: 184, right: 240, top: 8, bottom: 35 },
    ]);

    expect(lines).toEqual([{ left: 10, right: 240, top: 8, bottom: 35 }]);
  });

  it('keeps each line of a paragraph apart, from top to bottom', () => {
    const lines = selectionLines([
      { left: 10, right: 90, top: 40, bottom: 63 },
      { left: 200, right: 700, top: 10, bottom: 33 },
    ]);

    expect(lines.map((line) => line.top)).toEqual([10, 40]);
  });

  it('keeps apart two cells of a table on the same line', () => {
    const lines = selectionLines([
      { left: 10, right: 60, top: 10, bottom: 33 },
      { left: 200, right: 260, top: 10, bottom: 33 },
    ]);

    expect(lines).toHaveLength(2);
  });

  it('leaves out the empty boxes of a line break or of hidden text', () => {
    expect(selectionLines([
      { left: 10, right: 10.2, top: 10, bottom: 33 },
      { left: 10, right: 11, top: 10, bottom: 11 },
    ])).toEqual([]);
  });

  it('cuts a box to the part shown, and drops one scrolled out of sight', () => {
    const frame = { left: 0, right: 100, top: 0, bottom: 100 };

    expect(clipBox({ left: 80, right: 140, top: 10, bottom: 30 }, frame)).toEqual({ left: 80, right: 100, top: 10, bottom: 30 });
    expect(clipBox({ left: 120, right: 140, top: 10, bottom: 30 }, frame)).toBeNull();
    expect(clipBox({ left: 120, right: 140, top: 10, bottom: 30 }, null)).toEqual({ left: 120, right: 140, top: 10, bottom: 30 });
  });

  it('draws a line as a pill with round ends and a little room around the words', () => {
    expect(pillOf({ left: 10, right: 110, top: 20, bottom: 42 })).toEqual({ x: 4, y: 19, width: 112, height: 24, radius: 12 });
  });
});

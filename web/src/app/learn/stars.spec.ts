import { averageStars, starSummary, starTone } from './stars';

describe('stars', () => {
  it('shows gold stars for a question of the session, blue for one asked again, outlined for a free exercise', () => {
    expect(starTone({ hook: false, reused: false, free: false })).toBe('gold');
    expect(starTone({ hook: false, reused: true, free: false })).toBe('blue');
    expect(starTone({ hook: false, reused: false, free: true })).toBe('outline');
    expect(starTone({ hook: true, reused: false, free: false })).toBeNull();
  });

  it('counts only gold stars in the total, out of 3 for each gold question, answered or not', () => {
    const summary = starSummary(
      [
        { id: 'a1', tone: 'gold' },
        { id: 'a2', tone: 'gold' },
        { id: 'a3', tone: 'gold' },
        { id: 'w1', tone: 'blue' },
        { id: 'p1', tone: 'outline' },
        { id: 'h1', tone: null },
        { id: 'a1', tone: 'gold' },
      ],
      new Map([['a1', 3], ['a2', 1], ['w1', 2], ['p1', 3], ['h1', 3]]),
    );

    expect(summary).toEqual({ stars: 4, possible: 9, gold: [3, 1], blue: [2] });
  });

  it('takes the rounded mean of several answers', () => {
    expect(averageStars([3, 2, 2])).toBe(2);
    expect(averageStars([3, 3, 2])).toBe(3);
    expect(averageStars([])).toBe(0);
  });
});

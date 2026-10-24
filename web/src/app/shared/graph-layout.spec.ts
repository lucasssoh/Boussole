import { describe, expect, it } from 'vitest';
import { layOut, reduceLinks } from './graph-layout';

describe('graph layout', () => {
  it('leaves out a link another path already gives', () => {
    expect(reduceLinks(['a', 'b', 'c'], [['a', 'b'], ['b', 'c'], ['a', 'c']])).toEqual([['a', 'b'], ['b', 'c']]);
  });

  it('puts each node one floor below the lowest of those it waits for', () => {
    const layout = layOut(['a', 'b', 'c', 'd'], [['a', 'b'], ['a', 'c'], ['b', 'd'], ['c', 'd']]);

    expect([...layout.floor.entries()]).toEqual([['a', 0], ['b', 1], ['c', 1], ['d', 2]]);
    expect(layout.floors.map((slots) => slots.length)).toEqual([1, 2, 1]);
  });

  it('routes a link that skips a floor through a gap of it', () => {
    const layout = layOut(['a', 'b', 'c'], [['a', 'b'], ['b', 'c'], ['a', 'c']]);
    const skip = layout.routes.find((route) => route.link[1] === 'c' && route.link[0] === 'a')!;

    expect(skip.slots.map((slot) => slot.floor)).toEqual([0, 1, 2]);
    expect(layout.floors[1]).toHaveLength(2);
  });

  it('orders each floor so that no link crosses another when it can', () => {
    const layout = layOut(['a', 'b', 'c', 'd'], [['a', 'd'], ['b', 'c']]);

    expect(layout.crossings).toBe(0);
  });
});

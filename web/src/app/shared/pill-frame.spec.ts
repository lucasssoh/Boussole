import { describe, expect, it } from 'vitest';
import { pillFrame } from './pill-frame';
import { CULTURE_TIERS, SPECIALTY_TIERS } from './ranks';

describe('pill frame', () => {
  it('ornaments the frame more as the culture rises, rivets on top and bottom', () => {
    const frames = CULTURE_TIERS.map((tier) => pillFrame('culture', tier));
    const studs = frames.map((frame) => frame.studs.length);
    expect(studs[0]).toBe(0);
    expect([...studs].sort((a, b) => a - b)).toEqual(studs);
    expect(frames[7].studs.filter((stud) => stud.top)).toHaveLength(3);
    expect(frames.map((frame) => frame.gem)).toEqual([false, false, false, true, true, true, true, true]);
  });

  it('turns to night from Master on, as the emblems do', () => {
    expect(CULTURE_TIERS.map((tier) => pillFrame('culture', tier).dark)).toEqual([false, false, false, false, false, true, true, true]);
  });

  it('frames the last specialty with the spectrum, on a night sky', () => {
    const luminary = pillFrame('specialty', SPECIALTY_TIERS[6]);
    expect(luminary.vars['--frame']).toContain('#9775fa');
    expect(luminary.dark).toBe(true);
    expect(pillFrame('specialty', 'novice').vars['--stars']).toBe('none');
  });
});

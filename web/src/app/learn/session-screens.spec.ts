import { BlockView, SessionPart } from '../core/api';
import { sessionPlan } from './session-screens';

const prose = (markdown: string): BlockView => ({ type: 'prose', markdown });
const example: BlockView = { type: 'example', label: '', blocks: [] };
const mcq = (id: string): BlockView => ({
  type: 'mcq', id, multiple: false, statement: [], choices: ['a', 'b'], order: [1, 0], minutes: 1,
  hints: { total: 0, revealed: [] }, progress: { attempts: 0, solved: false, lastVerdict: null, answeredHere: null }, reused: false, hook: false,
});
const part = (kind: SessionPart['kind'], blocks: BlockView[]): SessionPart => ({ kind, id: null, title: null, blocks });
const long = 'x'.repeat(400);

describe('sessionPlan', () => {
  it('gives the explanation, the example and each question a screen, and ends with a closing screen', () => {
    const plan = sessionPlan([part('notion', [prose(long), mcq('q1'), example, mcq('q2')])]);
    expect(plan.screens.map((screen) => screen.kind)).toEqual(['start', 'explanation', 'question', 'example', 'question', 'end']);
    expect(plan.parts[0].screens).toEqual([1, 2, 3, 4]);
  });

  it('keeps a short lead with the question it introduces', () => {
    const plan = sessionPlan([part('warmup', [prose('Cinq minutes sur s33.'), mcq('w1'), mcq('w2')])]);
    expect(plan.screens.map((screen) => screen.kind)).toEqual(['start', 'question', 'question', 'end']);
    expect(plan.screens[1].blocks).toHaveLength(2);
  });

  it('opens the next part with the text between parts', () => {
    const plan = sessionPlan([part('warmup', [mcq('w1')]), part('text', [prose(long)]), part('notion', [mcq('q1')])]);
    expect(plan.parts.map((found) => found.part.kind)).toEqual(['warmup', 'notion']);
    expect(plan.screens.map((screen) => screen.kind)).toEqual(['start', 'question', 'explanation', 'question', 'end']);
  });

  it('closes the last part with the text after every part', () => {
    const plan = sessionPlan([part('notion', [mcq('q1')]), part('text', [prose(long)])]);
    expect(plan.parts).toHaveLength(1);
    expect(plan.screens.map((screen) => screen.kind)).toEqual(['start', 'question', 'explanation', 'end']);
  });
});

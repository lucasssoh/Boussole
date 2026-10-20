import { BlockView, SessionPart } from '../core/api';
import { sessionPages } from './session-pages';

const prose = (markdown: string): BlockView => ({ type: 'prose', markdown });
const mcq = (id: string): BlockView => ({
  type: 'mcq', id, multiple: false, statement: [], choices: ['a', 'b'], minutes: 1,
  hints: { total: 0, revealed: [] }, progress: { attempts: 0, solved: false, lastVerdict: null }, reused: false,
});
const part = (kind: SessionPart['kind'], blocks: BlockView[]): SessionPart => ({ kind, id: null, title: null, blocks });

describe('sessionPages', () => {
  it('makes one page per part, leaving out empty ones', () => {
    const pages = sessionPages([part('text', []), part('warmup', [mcq('w1')]), part('notion', [prose('x')])]);
    expect(pages.map((page) => page.part.kind)).toEqual(['warmup', 'notion']);
  });

  it('opens the next page with the text between parts', () => {
    const pages = sessionPages([part('warmup', [mcq('w1')]), part('text', [prose('intro')]), part('notion', [prose('x')])]);
    expect(pages.map((page) => page.part.kind)).toEqual(['warmup', 'notion']);
    expect(pages[1].steps[0]).toEqual([prose('intro'), prose('x')]);
  });

  it('closes the last page with the text after every part', () => {
    const pages = sessionPages([part('notion', [prose('x')]), part('text', [prose('end')])]);
    expect(pages).toHaveLength(1);
    expect(pages[0].steps[0]).toEqual([prose('x'), prose('end')]);
  });

  it('makes a page of a session that is only text', () => {
    expect(sessionPages([part('text', [prose('all')])]).map((page) => page.part.kind)).toEqual(['text']);
  });

  it('ends a step with each question', () => {
    const [page] = sessionPages([part('notion', [prose('a'), mcq('q1'), prose('b'), prose('c'), mcq('q2'), prose('d')])]);
    expect(page.steps.map((step) => step.length)).toEqual([2, 3, 1]);
  });
});

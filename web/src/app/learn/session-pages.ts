import { BlockView, SessionPart } from '../core/api';

/**
 * A session shown one page at a time, one page per part (warm-up, each notion, checkpoint,
 * practice), and each page revealed step by step: a step ends with a question, so the learner
 * answers it before reading on. Text outside any part, such as an introduction, opens the page
 * that follows it (or closes the last page) rather than making a page of its own.
 */
export interface SessionPage {
  part: SessionPart;
  steps: BlockView[][];
}

export function sessionPages(parts: SessionPart[]): SessionPage[] {
  const pages: SessionPage[] = [];
  let pending: BlockView[] = [];
  for (const part of parts.filter((found) => found.blocks.length > 0)) {
    if (part.kind === 'text') {
      pending = [...pending, ...part.blocks];
    } else {
      pages.push({ part, steps: steps([...pending, ...part.blocks]) });
      pending = [];
    }
  }
  if (pending.length > 0) {
    const last = pages.pop();
    pages.push(last
      ? { part: last.part, steps: steps([...last.part.blocks, ...pending]) }
      : { part: { kind: 'text', id: null, title: null, blocks: pending }, steps: steps(pending) });
  }
  return pages;
}

function steps(blocks: BlockView[]): BlockView[][] {
  const found: BlockView[][] = [];
  let current: BlockView[] = [];
  for (const block of blocks) {
    current.push(block);
    if (block.type === 'mcq' || block.type === 'exercise') {
      found.push(current);
      current = [];
    }
  }
  if (current.length > 0) {
    found.push(current);
  }
  return found;
}

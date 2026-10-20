import { BlockView, SessionPart } from '../core/api';

/** What a screen shows: the goal of the session, an explanation, the worked example, one question, or the end. */
export type ScreenKind = 'start' | 'explanation' | 'example' | 'question' | 'end';

export interface SessionScreen {
  kind: ScreenKind;
  /** The part of the session the screen belongs to, by its index in {@link SessionPlan.parts}. */
  part: number;
  blocks: BlockView[];
  /** The question of a question screen. */
  question?: { id: string; type: 'mcq' | 'exercise' };
}

export interface SessionPlanPart {
  part: SessionPart;
  /** The indexes of its screens in {@link SessionPlan.screens}. */
  screens: number[];
}

export interface SessionPlan {
  parts: SessionPlanPart[];
  screens: SessionScreen[];
}

/** Text shorter than this, just before a question or an example, is its lead rather than a screen of its own. */
const LEAD_LENGTH = 320;

/**
 * Cuts a session into screens, shown one at a time: within each part (warm-up, each notion,
 * checkpoint, practice), the explanation, the worked example and each question get their own
 * screen. A short text introducing a question or an example goes with it. Text outside any part
 * opens the next part (or closes the last one). A first screen states the goal of the session,
 * a last one closes it.
 */
export function sessionPlan(sessionParts: SessionPart[]): SessionPlan {
  const plan: SessionPlan = { parts: [], screens: [{ kind: 'start', part: -1, blocks: [] }] };
  for (const part of mergeLooseText(sessionParts)) {
    const index = plan.parts.length;
    const screens = screensOf(part.blocks, index);
    plan.parts.push({ part, screens: screens.map((_, offset) => plan.screens.length + offset) });
    plan.screens.push(...screens);
  }
  plan.screens.push({ kind: 'end', part: -1, blocks: [] });
  return plan;
}

function mergeLooseText(parts: SessionPart[]): SessionPart[] {
  const merged: SessionPart[] = [];
  let pending: BlockView[] = [];
  for (const part of parts.filter((found) => found.blocks.length > 0)) {
    if (part.kind === 'text') {
      pending = [...pending, ...part.blocks];
    } else {
      merged.push({ ...part, blocks: [...pending, ...part.blocks] });
      pending = [];
    }
  }
  if (pending.length > 0) {
    const last = merged.pop();
    merged.push(last ? { ...last, blocks: [...last.blocks, ...pending] } : { kind: 'text', id: null, title: null, blocks: pending });
  }
  return merged;
}

function screensOf(blocks: BlockView[], part: number): SessionScreen[] {
  const screens: SessionScreen[] = [];
  let text: BlockView[] = [];
  const lead = (): BlockView[] => {
    const short = isShortLead(text);
    const taken = short ? text : [];
    if (!short && text.length > 0) {
      screens.push({ kind: 'explanation', part, blocks: text });
    }
    text = [];
    return taken;
  };
  for (const block of blocks) {
    if (block.type === 'mcq' || block.type === 'exercise') {
      screens.push({ kind: 'question', part, blocks: [...lead(), block], question: { id: block.id, type: block.type } });
    } else if (block.type === 'example') {
      screens.push({ kind: 'example', part, blocks: [...lead(), block] });
    } else {
      text.push(block);
    }
  }
  if (text.length > 0) {
    screens.push({ kind: 'explanation', part, blocks: text });
  }
  return screens;
}

function isShortLead(blocks: BlockView[]): boolean {
  return blocks.every((block) => block.type === 'prose')
    && blocks.reduce((length, block) => length + (block.type === 'prose' ? block.markdown.length : 0), 0) < LEAD_LENGTH;
}

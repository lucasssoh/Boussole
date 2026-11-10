/**
 * The words that give an algorithm its structure, in French and in English, as `algorithmic` sets
 * them in LaTeX. Most open a line (« pour chaque », « si », « retourner »); a few close it
 * (« alors », « faire »). Only there are they keywords: « pour tout $i$ » inside a step stays text.
 * Longer phrases first, so that « pour chaque » is one keyword, not « pour ».
 */
const OPENING = [
  'pour chaque', 'pour tout', 'pour', 'tant que', 'sinon si', 'sinon', 'si',
  'fin pour', 'fin tant que', 'fin si', 'fin', 'répéter', "jusqu'à", 'jusqu’à', 'retourner', 'renvoyer',
  'entrées', 'entrée', 'sorties', 'sortie', 'début', 'faire',
  'for each', 'for all', 'for', 'while', 'else if', 'else', 'if',
  'end for', 'end while', 'end if', 'end', 'repeat', 'until', 'return', 'input', 'output', 'begin', 'do',
];
const CLOSING = ['alors', 'faire', 'then', 'do'];

const alternatives = (words: string[]) =>
  [...words]
    .sort((a, b) => b.length - a.length)
    .map((word) => word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/ /g, '\\s+'))
    .join('|');
const NOT_WORD = "[\\p{L}\\p{N}_'’]";
const LEADING = new RegExp(`^(\\s*)(${alternatives(OPENING)})(?!${NOT_WORD})`, 'iu');
const TRAILING = new RegExp(`(?<!${NOT_WORD})(${alternatives(CLOSING)})(\\s*)$`, 'iu');

/** Marks a keyword while the line goes through Markdown: characters Markdown neither escapes nor reads. */
export const KEYWORD_OPEN = '\uE010';
export const KEYWORD_CLOSE = '\uE011';

/** A line of an algorithm: the step, and the comment after `//` or `▷`, if any. */
export interface AlgorithmLineParts {
  step: string;
  comment: string | null;
}

/** The pieces of a line outside `$…$` math and `code`, where keywords and comments are looked for. */
function outsideMath(text: string): { text: string; math: boolean }[] {
  const pieces: { text: string; math: boolean }[] = [];
  const pattern = /\$[^$\n]+\$|`[^`\n]+`/g;
  let last = 0;
  for (const match of text.matchAll(pattern)) {
    if (match.index > last) {
      pieces.push({ text: text.slice(last, match.index), math: false });
    }
    pieces.push({ text: match[0], math: true });
    last = match.index + match[0].length;
  }
  if (last < text.length) {
    pieces.push({ text: text.slice(last), math: false });
  }
  return pieces;
}

/** Splits off the comment of a line: the first `//` or `▷` outside math. */
export function splitComment(text: string): AlgorithmLineParts {
  let offset = 0;
  for (const piece of outsideMath(text)) {
    if (!piece.math) {
      const at = piece.text.search(/\/\/|▷/);
      if (at >= 0) {
        const start = offset + at;
        const marker = text.startsWith('//', start) ? 2 : 1;
        return { step: text.slice(0, start).trimEnd(), comment: text.slice(start + marker).trim() };
      }
    }
    offset += piece.text.length;
  }
  return { step: text, comment: null };
}

/**
 * The step with its keywords between {@link KEYWORD_OPEN} and {@link KEYWORD_CLOSE}: the one that
 * opens the line and the one that closes it, never one inside math or in the middle of the step.
 */
export function markKeywords(step: string): string {
  const pieces = outsideMath(step);
  if (pieces.length === 0) {
    return step;
  }
  const first = pieces[0];
  if (!first.math) {
    first.text = first.text.replace(LEADING, `$1${KEYWORD_OPEN}$2${KEYWORD_CLOSE}`);
  }
  const last = pieces[pieces.length - 1];
  if (!last.math && !(pieces.length === 1 && first.text.endsWith(KEYWORD_CLOSE))) {
    last.text = last.text.replace(TRAILING, `${KEYWORD_OPEN}$1${KEYWORD_CLOSE}$2`);
  }
  return pieces.map((piece) => piece.text).join('');
}

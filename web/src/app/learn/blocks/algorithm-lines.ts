/**
 * The words that give an algorithm its structure, in French and in English, as `algorithmic` sets
 * them in LaTeX. Longer phrases first, so that « pour chaque » is one keyword, not « pour ».
 */
const KEYWORDS = [
  'pour chaque', 'pour tout', 'pour', 'tant que', 'faire', 'sinon si', 'sinon', 'si', 'alors',
  'fin pour', 'fin tant que', 'fin si', 'fin', 'répéter', "jusqu'à", 'jusqu’à', 'retourner', 'renvoyer',
  'entrées', 'entrée', 'sorties', 'sortie', 'début',
  'for each', 'for all', 'for', 'while', 'do', 'else if', 'else', 'if', 'then',
  'end for', 'end while', 'end if', 'end', 'repeat', 'until', 'return', 'input', 'output', 'begin',
].sort((a, b) => b.length - a.length);

const escaped = KEYWORDS.map((word) => word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/ /g, '\\s+'));
const KEYWORD = new RegExp(`(?<![\\p{L}\\p{N}_'’])(${escaped.join('|')})(?![\\p{L}\\p{N}_'’])`, 'giu');

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

/** The step with its keywords between {@link KEYWORD_OPEN} and {@link KEYWORD_CLOSE}, math left alone. */
export function markKeywords(step: string): string {
  return outsideMath(step)
    .map((piece) => (piece.math ? piece.text : piece.text.replace(KEYWORD, `${KEYWORD_OPEN}$1${KEYWORD_CLOSE}`)))
    .join('');
}

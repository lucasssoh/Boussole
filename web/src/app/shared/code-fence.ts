import hljs from 'highlight.js/lib/core';
import bash from 'highlight.js/lib/languages/bash';
import c from 'highlight.js/lib/languages/c';
import cpp from 'highlight.js/lib/languages/cpp';
import java from 'highlight.js/lib/languages/java';
import javascript from 'highlight.js/lib/languages/javascript';
import json from 'highlight.js/lib/languages/json';
import ocaml from 'highlight.js/lib/languages/ocaml';
import python from 'highlight.js/lib/languages/python';
import sql from 'highlight.js/lib/languages/sql';
import typescript from 'highlight.js/lib/languages/typescript';
import xml from 'highlight.js/lib/languages/xml';
import yaml from 'highlight.js/lib/languages/yaml';

hljs.registerLanguage('bash', bash);
hljs.registerLanguage('c', c);
hljs.registerLanguage('cpp', cpp);
hljs.registerLanguage('java', java);
hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('json', json);
hljs.registerLanguage('ocaml', ocaml);
hljs.registerLanguage('python', python);
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('yaml', yaml);

/** What the info string of a fence asks for: ```` ```java {numbers} ```` or ```` ```java {3,5-6} ````. */
export interface FenceInfo {
  /** The language, in lower case; empty when none is given. */
  language: string;
  /** Numbered lines: asked with `numbers`, or by marking lines, since a marked line is pointed at by its number. */
  numbered: boolean;
  /** The lines to mark, counted from 1. */
  marked: Set<number>;
}

const INFO = /^([^\s{]*)\s*(?:\{([^}]*)\})?/;

export function parseFenceInfo(info: string): FenceInfo {
  const match = INFO.exec(info.trim());
  const language = (match?.[1] ?? '').toLowerCase();
  const options = (match?.[2] ?? '').split(/[\s,]+/).filter((option) => option !== '');
  const marked = new Set<number>();
  for (const option of options) {
    const range = /^(\d+)(?:-(\d+))?$/.exec(option);
    if (range) {
      const from = Number(range[1]);
      const to = Number(range[2] ?? range[1]);
      for (let line = from; line <= to && line - from < 1000; line++) {
        marked.add(line);
      }
    }
  }
  return { language, numbered: options.includes('numbers') || marked.size > 0, marked };
}

/** A language the code blocks colour; any other shows as plain text. */
export function knownLanguage(language: string): boolean {
  return language !== '' && hljs.getLanguage(language) !== undefined;
}

/**
 * Cuts highlighted HTML into its lines, closing at the end of a line the spans still open and
 * opening them again on the next, so that a comment over several lines keeps its colour.
 */
export function splitLines(html: string): string[] {
  const lines: string[] = [];
  const open: string[] = [];
  let current = '';
  for (const [piece] of html.matchAll(/<span[^>]*>|<\/span>|\n|[^<\n]+|</g)) {
    if (piece === '\n') {
      lines.push(current + '</span>'.repeat(open.length));
      current = open.join('');
    } else if (piece.startsWith('<span')) {
      open.push(piece);
      current += piece;
    } else if (piece === '</span>') {
      open.pop();
      current += piece;
    } else {
      current += piece;
    }
  }
  lines.push(current);
  return lines;
}

/** Stands for the gap N while the code is coloured: characters no language colours or cuts. */
const GAP_OPEN = '';
const GAP_CLOSE = '';

/**
 * A code block: coloured when its language is known, one span per line (numbered and marked as the
 * info string asks), the gaps of a fill exercise as fields when {@code gaps} is true.
 */
export function renderCodeFence(content: string, info: string, gaps: boolean, escape: (text: string) => string): string {
  const fence = parseFenceInfo(info);
  const source = (gaps ? content.replace(/\[\[(\d+)\]\]/g, (_, number: string) => `${GAP_OPEN}${number}${GAP_CLOSE}`) : content).replace(/\n$/, '');
  const coloured = knownLanguage(fence.language);
  const html = coloured ? hljs.highlight(source, { language: fence.language, ignoreIllegals: true }).value : escape(source);
  const lines = splitLines(html).map((line, index) => {
    // A language may colour the number of a gap: the span it puts around it goes with the gap.
    const withGaps = line.replace(new RegExp(`${GAP_OPEN}(?:<span[^>]*>)?(\\d+)(?:</span>)?${GAP_CLOSE}`, 'g'), (_, number: string) => `<span class="gap" data-gap="${Number(number)}"></span>`);
    return `<span class="line${fence.marked.has(index + 1) ? ' marked' : ''}">${withGaps === '' ? '​' : withGaps}</span>`;
  });
  const classes = ['code', fence.numbered ? 'numbered' : '', coloured ? 'hljs' : ''].filter((name) => name !== '').join(' ');
  const language = fence.language === '' ? '' : ` data-language="${escape(fence.language)}"`;
  return `<div class="code-block"><pre class="${classes}"${language}><code>${lines.join('')}</code></pre></div>\n`;
}

/** The text of a code block as written, line by line, without its numbers. */
export function codeText(block: Element): string {
  return Array.from(block.querySelectorAll('.line'), (line) => (line.textContent ?? '').replace(/​/g, '')).join('\n');
}

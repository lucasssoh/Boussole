import { Injectable } from '@angular/core';
import MarkdownItCallable, { type MarkdownIt, type RendererRule, type StateInline } from 'markdown-it';
import katexPlugin from '@vscode/markdown-it-katex';
import { iconSvg } from './icon.component';

/** What a piece of Markdown is rendered against: the folder its relative links and images start from. */
export interface MarkdownContext {
  /** The URL of the folder of the session file, ending with a slash. */
  base: string;
  /** The statement of a fill exercise: its [[N]] are gaps, in the text and in code. Elsewhere they stay as written. */
  gaps?: boolean;
}

const DIRECTIVE = /^:(term|pdf|link)\[([^\]\n]*)\](?:\{([^}\n]*)\})?/;
const GAP = /^\[\[(\d+)\]\]/;
const ATTRIBUTE = /([#.]?)([\w-]+)(?:=(?:"([^"]*)"|'([^']*)'|([^\s"']+)))?/g;

/**
 * Renders the prose of a session: Markdown, maths ($…$ and $$…$$ with KaTeX), the inline
 * directives of the format (:term, :pdf and :link), the gaps of a fill exercise ([[N]]) and Mermaid
 * diagrams. Raw HTML in the source is shown as text, never run.
 */
@Injectable({ providedIn: 'root' })
export class MarkdownRenderer {
  private readonly markdown = createMarkdown();

  render(source: string, context: MarkdownContext): string {
    return this.markdown.render(source, { ...context });
  }

  /** One line of Markdown, without a paragraph around it: a title, a goal, a choice of an MCQ. */
  renderInline(source: string): string {
    return this.markdown.renderInline(source, {});
  }
}

function createMarkdown(): MarkdownIt {
  const markdown: MarkdownIt = new MarkdownItCallable({ html: false, linkify: true });
  const katex = (katexPlugin as unknown as { default?: typeof katexPlugin }).default ?? katexPlugin;
  markdown.use(katex as never, { throwOnError: false });
  markdown.inline.ruler.before('link', 'boussole_directive', directiveRule);
  markdown.inline.ruler.before('link', 'boussole_gap', gapRule);
  markdown.renderer.rules['boussole_directive'] = (tokens, index) => renderDirective(tokens[index].meta as unknown as DirectiveMeta, markdown);
  markdown.renderer.rules['boussole_gap'] = (tokens, index) => `<span class="gap" data-gap="${Number(tokens[index].meta?.['number'])}"></span>`;
  resolveRelativeUrls(markdown);
  renderMermaid(markdown);
  gapsInCode(markdown);
  return markdown;
}

/**
 * A gap may sit in code too, to complete a line of a program: `for i in [[1]](10)`. Code is
 * rendered as it is written, so its gaps become fields after the code is escaped.
 */
function gapsInCode(markdown: MarkdownIt): void {
  const withGaps = (rule: RendererRule): RendererRule => (tokens, index, options, env, self) =>
    !(env as MarkdownContext | undefined)?.gaps ? rule(tokens, index, options, env, self) : rule(tokens, index, options, env, self).replace(/\[\[(\d+)\]\]/g, (_, number: string) => `<span class="gap" data-gap="${Number(number)}"></span>`);
  markdown.renderer.rules['code_inline'] = withGaps(markdown.renderer.rules['code_inline']!);
  const fence = markdown.renderer.rules['fence']!;
  markdown.renderer.rules['fence'] = (tokens, index, options, env, self) =>
    tokens[index].info.trim() === 'mermaid' ? fence(tokens, index, options, env, self) : withGaps(fence)(tokens, index, options, env, self);
}

function directiveRule(state: StateInline, silent: boolean): boolean {
  if (state.src.charCodeAt(state.pos) !== 0x3a /* : */) {
    return false;
  }
  const match = DIRECTIVE.exec(state.src.slice(state.pos));
  if (!match) {
    return false;
  }
  if (!silent) {
    const token = state.push('boussole_directive', '', 0);
    token.meta = { name: match[1], text: match[2], attributes: attributes(match[3] ?? '') };
  }
  state.pos += match[0].length;
  return true;
}

function gapRule(state: StateInline, silent: boolean): boolean {
  if (state.src.charCodeAt(state.pos) !== 0x5b /* [ */ || !(state.env as Partial<MarkdownContext> | undefined)?.gaps) {
    return false;
  }
  const match = GAP.exec(state.src.slice(state.pos));
  if (!match) {
    return false;
  }
  if (!silent) {
    state.push('boussole_gap', '', 0).meta = { number: Number(match[1]) };
  }
  state.pos += match[0].length;
  return true;
}

function attributes(text: string): Record<string, string> {
  const found: Record<string, string> = {};
  for (const match of text.matchAll(ATTRIBUTE)) {
    const key = match[1] === '#' ? 'id' : match[2];
    found[key] = match[1] === '#' ? match[2] : (match[3] ?? match[4] ?? match[5] ?? '');
  }
  return found;
}

interface DirectiveMeta {
  name: string;
  text: string;
  attributes: Record<string, string>;
}

function renderDirective(meta: DirectiveMeta, markdown: MarkdownIt): string {
  const escape = markdown.utils.escapeHtml;
  const text = markdown.renderInline(meta.text);
  if (meta.name === 'term') {
    return `<span class="term" tabindex="0" data-term-id="${escape(meta.attributes['id'] ?? '')}" data-term-text="${escape(meta.text)}">${text}</span>`;
  }
  if (meta.name === 'link') {
    return `<button type="button" class="pdf-link web-link" data-link-url="${escape(meta.attributes['url'] ?? '')}">${iconSvg('globe', 16)}<span class="pdf-label">${text}</span></button>`;
  }
  return `<button type="button" class="pdf-link" data-pdf-src="${escape(meta.attributes['src'] ?? '')}" data-pdf-page="${escape(meta.attributes['page'] ?? '')}">${iconSvg('document', 16)}<span class="pdf-label">${text}</span></button>`;
}

/** Images and links relative to the session file, such as ../assets/figure.svg, resolve against its folder. */
function resolveRelativeUrls(markdown: MarkdownIt): void {
  const image = markdown.renderer.rules['image']!;
  const relativeImage: RendererRule = (tokens, index, options, env, self) => {
    const token = tokens[index];
    token.attrSet('src', resolved(String(token.attrGet('src') ?? ''), env as MarkdownContext | undefined));
    token.attrSet('loading', 'lazy');
    return image(tokens, index, options, env, self);
  };
  const linkOpen: RendererRule = (tokens, index, options, env, self) => {
    const token = tokens[index];
    const href = String(token.attrGet('href') ?? '');
    if (/^[a-z]+:/i.test(href)) {
      token.attrSet('target', '_blank');
      token.attrSet('rel', 'noopener');
    } else if (!href.startsWith('#')) {
      token.attrSet('href', resolved(href, env as MarkdownContext | undefined));
    }
    return self.renderToken(tokens, index, options);
  };
  markdown.renderer.rules['image'] = relativeImage;
  markdown.renderer.rules['link_open'] = linkOpen;
}

function resolved(url: string, context: MarkdownContext | undefined): string {
  if (/^([a-z]+:|\/|#)/i.test(url) || !context?.base) {
    return url;
  }
  const absolute = new URL(url, new URL(context.base, location.origin));
  return absolute.pathname + absolute.search + absolute.hash;
}

function renderMermaid(markdown: MarkdownIt): void {
  const fence = markdown.renderer.rules['fence']!;
  const mermaidFence: RendererRule = (tokens, index, options, env, self) => {
    const token = tokens[index];
    return token.info.trim() === 'mermaid'
      ? `<pre class="mermaid">${markdown.utils.escapeHtml(token.content)}</pre>`
      : fence(tokens, index, options, env, self);
  };
  markdown.renderer.rules['fence'] = mermaidFence;
}

import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, effect, inject, input } from '@angular/core';
import { MarkdownRenderer } from '../../shared/markdown-renderer';
import { drawMermaidDiagrams } from '../../shared/mermaid-diagrams';
import { LearnFacade } from '../learn.facade';
import { WebReferences } from '../web-references';
import { FillGaps } from '../question/fill-gaps';

/**
 * Markdown of the session. Once rendered, its :term get their definition from the lexicon, its
 * :pdf open the document beside the text, and the gaps of a fill exercise get their fields. Its
 * tables get a frame as wide as the screen allows, and a definition is placed against the window,
 * so that nothing around it, a table or a frame, cuts it.
 */
@Component({
  selector: 'app-prose-block',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'prose',
    '(click)': 'onClick($event)',
    '(mouseover)': 'showTip($event)',
    '(focusin)': 'showTip($event)',
    '(mouseout)': 'hideTip($event)',
    '(focusout)': 'hideTip($event)',
    '(window:scroll)': 'replaceTip()',
    '(window:resize)': 'replaceTip()',
  },
  template: '',
})
export class ProseBlockComponent {
  readonly markdown = input.required<string>();
  readonly fill = input<FillGaps | null>(null);
  readonly base = input<string | null>(null);

  private readonly host: HTMLElement = inject(ElementRef).nativeElement;
  private readonly renderer = inject(MarkdownRenderer);
  private readonly learn = inject(LearnFacade);
  private readonly references = inject(WebReferences);
  /** The term whose definition shows, kept in place while the page scrolls. */
  private shownTerm: HTMLElement | null = null;
  /** The definitions, in the body of the page while this text is shown: above everything. */
  private tips = new Map<HTMLElement, HTMLElement>();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.removeTips());
    effect(() => {
      this.removeTips();
      this.host.innerHTML = this.renderer.render(this.markdown(), { base: this.base() ?? this.learn.sessionBase(), gaps: this.fill() !== null });
      this.widenTables();
      this.defineTerms();
      this.lookAtWebLinks();
      this.fill()?.mountIn(this.host);
      void drawMermaidDiagrams(this.host);
    });
  }

  protected onClick(event: MouseEvent): void {
    const web = (event.target as HTMLElement).closest<HTMLElement>('.web-link');
    if (web) {
      this.references.open(web.querySelector('.pdf-label')?.textContent?.trim() ?? '', web.dataset['linkUrl'] ?? '');
      return;
    }
    const link = (event.target as HTMLElement).closest<HTMLElement>('.pdf-link');
    if (link) {
      const page = Number(link.dataset['pdfPage']);
      this.learn.panel.set({
        kind: 'pdf',
        label: link.querySelector('.pdf-label')?.textContent?.trim() ?? '',
        path: this.learn.sourcePath(link.dataset['pdfSrc'] ?? ''),
        page: Number.isInteger(page) && page > 0 ? page : null,
      });
    }
  }

  protected showTip(event: Event): void {
    const term = (event.target as HTMLElement).closest<HTMLElement>('.term');
    if (term && term !== this.shownTerm) {
      if (this.shownTerm) {
        this.tips.get(this.shownTerm)?.classList.remove('shown');
      }
      this.shownTerm = term;
      this.replaceTip();
      const tip = this.tips.get(term);
      requestAnimationFrame(() => tip?.classList.toggle('shown', this.shownTerm === term));
    }
  }

  protected hideTip(event: FocusEvent | MouseEvent): void {
    const term = (event.target as HTMLElement).closest<HTMLElement>('.term');
    if (term && term === this.shownTerm && !term.contains(event.relatedTarget as Node | null)) {
      this.tips.get(term)?.classList.remove('shown');
      this.shownTerm = null;
    }
  }

  /** Under the term, or above it near the bottom of the window, never past its sides. */
  protected replaceTip(): void {
    const tip = this.shownTerm ? this.tips.get(this.shownTerm) : undefined;
    if (!this.shownTerm || !tip) {
      return;
    }
    const term = this.shownTerm.getClientRects()[0] ?? this.shownTerm.getBoundingClientRect();
    const margin = 8;
    const below = term.bottom + 6;
    const above = below + tip.offsetHeight > window.innerHeight - margin && term.top - tip.offsetHeight - 6 > margin;
    tip.style.left = `${Math.max(margin, Math.min(term.left, window.innerWidth - tip.offsetWidth - margin))}px`;
    tip.style.top = `${above ? term.top - tip.offsetHeight - 6 : below}px`;
    tip.classList.toggle('above', above);
  }

  private removeTips(): void {
    this.tips.forEach((tip) => tip.remove());
    this.tips.clear();
    this.shownTerm = null;
  }

  private widenTables(): void {
    for (const table of Array.from(this.host.querySelectorAll('table'))) {
      const frame = document.createElement('div');
      frame.className = 'table-frame';
      table.replaceWith(frame);
      frame.append(table);
    }
  }

  /** The web pages cited in the text are looked at now, so that a click knows where they open. */
  private lookAtWebLinks(): void {
    for (const link of Array.from(this.host.querySelectorAll<HTMLElement>('.web-link'))) {
      void this.references.preview(link.dataset['linkUrl'] ?? '');
    }
  }

  private defineTerms(): void {
    for (const element of Array.from(this.host.querySelectorAll<HTMLElement>('.term'))) {
      const term = this.learn.term(element.dataset['termId'] ?? '', element.dataset['termText'] ?? '');
      if (term) {
        const tip = document.createElement('span');
        tip.className = 'term-tip';
        tip.setAttribute('role', 'tooltip');
        tip.innerHTML = `<strong class="tip-term">${this.renderer.renderInline(term.term)}</strong>`
          + `<span class="tip-definition">${this.renderer.renderInline(term.definition)}</span>`;
        // Out of the text, the tip keeps the colour of the track.
        const style = getComputedStyle(this.host);
        for (const colour of ['--subject', '--subject-ink']) {
          tip.style.setProperty(colour, style.getPropertyValue(colour));
        }
        document.body.append(tip);
        this.tips.set(element, tip);
        element.setAttribute('aria-description', term.definition);
      }
    }
  }
}

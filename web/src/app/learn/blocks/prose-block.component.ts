import { ChangeDetectionStrategy, Component, ElementRef, effect, inject, input } from '@angular/core';
import { MarkdownRenderer } from '../../shared/markdown-renderer';
import { drawMermaidDiagrams } from '../../shared/mermaid-diagrams';
import { LearnFacade } from '../learn.facade';
import { WebReferences } from '../web-references';
import { FillGaps } from '../question/fill-gaps';

/**
 * Markdown of the session. Once rendered, its :term get their definition from the lexicon, its
 * :pdf open the document beside the text, and the gaps of a fill exercise get their fields.
 */
@Component({
  selector: 'app-prose-block',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'prose', '(click)': 'onClick($event)' },
  template: '',
})
export class ProseBlockComponent {
  readonly markdown = input.required<string>();
  readonly fill = input<FillGaps | null>(null);

  private readonly host: HTMLElement = inject(ElementRef).nativeElement;
  private readonly renderer = inject(MarkdownRenderer);
  private readonly learn = inject(LearnFacade);
  private readonly references = inject(WebReferences);

  constructor() {
    effect(() => {
      this.host.innerHTML = this.renderer.render(this.markdown(), { base: this.learn.sessionBase(), gaps: this.fill() !== null });
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
        element.append(tip);
        element.setAttribute('aria-description', term.definition);
      }
    }
  }
}

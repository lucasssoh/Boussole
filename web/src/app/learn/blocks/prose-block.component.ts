import { ChangeDetectionStrategy, Component, ElementRef, effect, inject, input } from '@angular/core';
import { MarkdownRenderer } from '../../shared/markdown-renderer';
import { drawMermaidDiagrams } from '../../shared/mermaid-diagrams';
import { LearnFacade } from '../learn.facade';
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

  constructor() {
    effect(() => {
      this.host.innerHTML = this.renderer.render(this.markdown(), { base: this.learn.sessionBase() });
      this.defineTerms();
      this.fill()?.mountIn(this.host);
      void drawMermaidDiagrams(this.host);
    });
  }

  protected onClick(event: MouseEvent): void {
    const link = (event.target as HTMLElement).closest<HTMLElement>('.pdf-link');
    if (link) {
      const page = Number(link.dataset['pdfPage']);
      this.learn.panel.set({
        kind: 'pdf',
        label: link.textContent?.replace('📄', '').trim() ?? '',
        path: this.learn.sourcePath(link.dataset['pdfSrc'] ?? ''),
        page: Number.isInteger(page) && page > 0 ? page : null,
      });
    }
  }

  private defineTerms(): void {
    for (const element of Array.from(this.host.querySelectorAll<HTMLElement>('.term'))) {
      const term = this.learn.term(element.dataset['termId'] ?? '', element.dataset['termText'] ?? '');
      if (term) {
        const tip = document.createElement('span');
        tip.className = 'term-tip';
        tip.setAttribute('role', 'tooltip');
        tip.innerHTML = `<strong>${this.renderer.renderInline(term.term)}</strong> : ${this.renderer.renderInline(term.definition)}`;
        element.append(tip);
        element.setAttribute('aria-description', term.definition);
      }
    }
  }
}

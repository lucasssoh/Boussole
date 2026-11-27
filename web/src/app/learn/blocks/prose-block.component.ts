import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, effect, inject, input, untracked } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { BlockView } from '../../core/api';
import { codeText } from '../../shared/code-fence';
import { iconSvg } from '../../shared/icon.component';
import { MarkdownRenderer } from '../../shared/markdown-renderer';
import { drawMermaidDiagrams } from '../../shared/mermaid-diagrams';
import { LearnFacade } from '../learn.facade';
import { PinnablePiece, PinnedLandmarks, landmarkKey, pinnablePieces } from '../landmarks';
import { sourcePanel } from '../session-documents';
import { WebReferences } from '../web-references';
import { FillGaps } from '../question/fill-gaps';

/**
 * Markdown of the session. Once rendered, its :term get their definition from the lexicon, its
 * :pdf open the document beside the text, and the gaps of a fill exercise get their fields. Its
 * tables get a frame as wide as the screen allows, and a definition is placed against the window,
 * so that nothing around it, a table or a frame, cuts it. Each code block gets a button that copies
 * its code, to take it into one's own editor.
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
  private readonly transloco = inject(TranslocoService, { optional: true });
  private readonly landmarks = inject(PinnedLandmarks);
  /** The formulas set apart and the tables of the text, each with the button that keeps it in view. */
  private pieces: { button: HTMLButtonElement; piece: PinnablePiece }[] = [];
  /** The term whose definition shows, kept in place while the page scrolls. */
  private shownTerm: HTMLElement | null = null;
  /** The definitions, in the body of the page while this text is shown: above everything. */
  private tips = new Map<HTMLElement, HTMLElement>();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.removeTips());
    effect(() => {
      this.landmarks.pinned();
      untracked(() => this.markPinned());
    });
    effect(() => {
      this.removeTips();
      this.host.innerHTML = this.renderer.render(this.markdown(), { base: this.base() ?? this.learn.sessionBase(), gaps: this.fill() !== null });
      this.widenTables();
      this.defineTerms();
      this.lookAtWebLinks();
      this.addCopyButtons();
      untracked(() => this.addPinButtons());
      untracked(() => this.fill()?.mountIn(this.host));
      void drawMermaidDiagrams(this.host);
    });
  }

  protected onClick(event: MouseEvent): void {
    const pin = (event.target as HTMLElement).closest<HTMLButtonElement>('.piece-pin');
    if (pin) {
      const found = this.pieces.find((entry) => entry.button === pin);
      if (found) {
        this.landmarks.toggle(this.transloco?.translate(found.piece.kind === 'math' ? 'landmarks.formula' : 'landmarks.table') ?? '', this.blocksOf(found.piece));
      }
      return;
    }
    const copy = (event.target as HTMLElement).closest<HTMLButtonElement>('.code-copy');
    if (copy) {
      this.copyCode(copy);
      return;
    }
    const web = (event.target as HTMLElement).closest<HTMLElement>('.web-link');
    if (web) {
      this.references.open(web.querySelector('.pdf-label')?.textContent?.trim() ?? '', web.dataset['linkUrl'] ?? '');
      return;
    }
    const link = (event.target as HTMLElement).closest<HTMLElement>('.pdf-link');
    if (link) {
      const page = Number(link.dataset['pdfPage']);
      this.learn.panel.set(sourcePanel(
        link.querySelector('.pdf-label')?.textContent?.trim() ?? '',
        this.learn.sourcePath(link.dataset['pdfSrc'] ?? ''),
        Number.isInteger(page) && page > 0 ? page : null,
      ));
    }
  }

  /**
   * A formula set apart or a table gets a button that keeps it in view beside the steps. The n-th
   * formula drawn is the n-th of the Markdown, and so for tables: each is pinned from its source.
   */
  private addPinButtons(): void {
    const sources = pinnablePieces(this.markdown());
    const drawn = { math: Array.from(this.host.querySelectorAll<HTMLElement>('.katex-display')), table: Array.from(this.host.querySelectorAll<HTMLElement>('.table-frame')) };
    this.pieces = [];
    for (const kind of ['math', 'table'] as const) {
      const pieces = sources.filter((piece) => piece.kind === kind);
      if (pieces.length !== drawn[kind].length) {
        continue;
      }
      drawn[kind].forEach((element, index) => {
        const label = this.transloco?.translate('landmarks.pin') ?? '';
        element.classList.add('pinnable-piece');
        element.insertAdjacentHTML('afterbegin', `<button type="button" class="piece-pin pin-button quiet" title="${label}" aria-label="${label}">${iconSvg('pin', 16)}</button>`);
        this.pieces.push({ button: element.querySelector<HTMLButtonElement>('.piece-pin')!, piece: pieces[index] });
      });
    }
    this.markPinned();
  }

  private markPinned(): void {
    for (const { button, piece } of this.pieces) {
      const pinned = this.landmarks.isPinned(landmarkKey(this.blocksOf(piece)));
      button.classList.toggle('pinned', pinned);
      button.setAttribute('aria-pressed', String(pinned));
      const label = this.transloco?.translate(pinned ? 'landmarks.unpin' : 'landmarks.pin') ?? '';
      button.title = label;
      button.setAttribute('aria-label', label);
    }
  }

  private blocksOf(piece: PinnablePiece): BlockView[] {
    return [{ type: 'prose', markdown: piece.markdown }];
  }

  private addCopyButtons(): void {
    const label = this.transloco?.translate('block.copy') ?? '';
    for (const block of Array.from(this.host.querySelectorAll('.code-block'))) {
      block.insertAdjacentHTML('afterbegin', `<button type="button" class="code-copy quiet" title="${label}" aria-label="${label}">${iconSvg('copy', 16)}<span>${label}</span></button>`);
    }
  }

  private copyCode(button: HTMLButtonElement): void {
    const block = button.closest('.code-block');
    if (!block || !navigator.clipboard) {
      return;
    }
    void navigator.clipboard.writeText(codeText(block)).then(() => {
      const copied = this.transloco?.translate('block.copied') ?? '';
      const label = button.querySelector('span');
      if (label) {
        label.textContent = copied;
      }
      button.classList.add('copied');
      setTimeout(() => {
        button.classList.remove('copied');
        if (label) {
          label.textContent = this.transloco?.translate('block.copy') ?? '';
        }
      }, 2000);
    });
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

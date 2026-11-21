import { DestroyRef, Directive, ElementRef, afterNextRender, inject } from '@angular/core';
import { Box, clipBox, pillOf, selectionLines } from './selection-pills';

const SVG = 'http://www.w3.org/2000/svg';

/** Text the pills leave to the plain selection colour: code, drawings, fields, hidden text. */
const PLAIN = 'pre, svg, input, textarea, .katex-mathml, .visually-hidden';

/**
 * Draws the text the learner selects as yellow pills, one per line, instead of the browser's
 * rectangles: many read by selecting the sentence they are on. The browser's own selection is
 * made transparent in the element (styles.css), and a layer of pills is drawn over the text, in
 * multiply, so that the letters stay as dark as they are.
 */
@Directive({
  selector: '[appSelectionPills]',
  host: { class: 'selection-pills' },
})
export class SelectionPillsDirective {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private layer: SVGSVGElement | null = null;
  private frame = 0;

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const layer = document.createElementNS(SVG, 'svg');
      layer.classList.add('selection-pills-layer');
      layer.setAttribute('aria-hidden', 'true');
      this.host.appendChild(layer);
      this.layer = layer;

      const schedule = () => {
        cancelAnimationFrame(this.frame);
        this.frame = requestAnimationFrame(() => this.draw());
      };
      document.addEventListener('selectionchange', schedule);
      this.host.addEventListener('scroll', schedule, { capture: true, passive: true });
      const resize = new ResizeObserver(schedule);
      resize.observe(this.host);
      schedule();

      destroyRef.onDestroy(() => {
        cancelAnimationFrame(this.frame);
        document.removeEventListener('selectionchange', schedule);
        this.host.removeEventListener('scroll', schedule, { capture: true });
        resize.disconnect();
        layer.remove();
      });
    });
  }

  private draw(): void {
    const layer = this.layer;
    if (!layer) {
      return;
    }
    const selection = document.getSelection();
    if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
      layer.replaceChildren();
      return;
    }
    const range = selection.getRangeAt(0);
    if (!range.intersectsNode(this.host)) {
      layer.replaceChildren();
      return;
    }
    const origin = layer.getBoundingClientRect();
    const boxes = this.textBoxes(range).map((box) => ({
      left: box.left - origin.left,
      right: box.right - origin.left,
      top: box.top - origin.top,
      bottom: box.bottom - origin.top,
    }));
    layer.replaceChildren(...selectionLines(boxes).map((line) => {
      const pill = pillOf(line);
      const rect = document.createElementNS(SVG, 'rect');
      rect.setAttribute('x', String(pill.x));
      rect.setAttribute('y', String(pill.y));
      rect.setAttribute('width', String(pill.width));
      rect.setAttribute('height', String(pill.height));
      rect.setAttribute('rx', String(pill.radius));
      return rect;
    }));
  }

  /** The boxes of the selected words of this element, one per piece of text and per line. */
  private textBoxes(range: Range): Box[] {
    // A walker never gives its own root: inside one piece of text, start from its element.
    const common = range.commonAncestorContainer;
    const element = common.nodeType === Node.TEXT_NODE ? common.parentNode! : common;
    const root = this.host.contains(element) ? element : this.host;
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
      acceptNode: (node) => {
        const parent = node.parentElement;
        const drawn = node.textContent?.trim() && parent && range.intersectsNode(node)
          && !parent.closest(PLAIN) && parent.closest('.selection-pills') === this.host;
        return drawn ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_REJECT;
      },
    });
    const piece = document.createRange();
    const clips = new Map<Element, Box | null>();
    const boxes: Box[] = [];
    while (walker.nextNode()) {
      const text = walker.currentNode as Text;
      const start = text === range.startContainer ? range.startOffset : 0;
      const end = text === range.endContainer ? range.endOffset : text.length;
      if (end <= start) {
        continue;
      }
      piece.setStart(text, start);
      piece.setEnd(text, end);
      const within = this.clipOf(text.parentElement!, clips);
      for (const rect of piece.getClientRects()) {
        const box = clipBox(rect, within);
        if (box) {
          boxes.push(box);
        }
      }
    }
    return boxes;
  }

  /** What is shown of an element: the boxes of the parents that hide what overflows, crossed. */
  private clipOf(element: Element, clips: Map<Element, Box | null>): Box | null {
    if (element === this.host || !this.host.contains(element)) {
      return null;
    }
    if (clips.has(element)) {
      return clips.get(element)!;
    }
    const above = this.clipOf(element.parentElement!, clips);
    const style = getComputedStyle(element);
    let own: Box | null = above;
    if (style.overflowX !== 'visible' || style.overflowY !== 'visible') {
      const rect = element.getBoundingClientRect();
      own = clipBox(rect, above) ?? { left: 0, right: 0, top: 0, bottom: 0 };
    }
    clips.set(element, own);
    return own;
  }
}

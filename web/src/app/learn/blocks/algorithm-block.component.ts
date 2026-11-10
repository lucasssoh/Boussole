import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { TranslocoPipe } from '@jsverse/transloco';
import { AlgorithmBlockView } from '../../core/api';
import { MarkdownRenderer } from '../../shared/markdown-renderer';
import { RichTextComponent } from '../../shared/rich-text.component';
import { KEYWORD_CLOSE, KEYWORD_OPEN, markKeywords, splitComment } from './algorithm-lines';

/**
 * Pseudo-code: numbered lines, so that a question may point to « la ligne 3 », indented with a
 * guide per level, the keywords in bold and in colour, the comments set apart. It keeps its
 * colours in a track whose code is in black and white: only code imitates the printed exam.
 */
@Component({
  selector: 'app-algorithm-block',
  imports: [RichTextComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <figure class="frame algorithm">
      <figcaption class="frame-title">{{ 'block.algorithm' | transloco }}@if (algorithm().label) { — <app-rich-text [text]="algorithm().label" />}</figcaption>
      <ol class="algorithm-lines">
        @for (line of lines(); track $index) {
          <li [style.--depth]="line.depth">
            <span class="algorithm-step" [innerHTML]="line.step"></span>
            @if (line.comment) {
              <span class="algorithm-comment" [innerHTML]="line.comment"></span>
            }
          </li>
        }
      </ol>
    </figure>
  `,
})
export class AlgorithmBlockComponent {
  readonly algorithm = input.required<AlgorithmBlockView>();

  private readonly renderer = inject(MarkdownRenderer);
  /** Markdown is rendered without raw HTML ({@link MarkdownRenderer}); KaTeX needs its inline styles. */
  private readonly sanitizer = inject(DomSanitizer);

  protected readonly lines = computed(() =>
    this.algorithm().lines.map((line) => {
      const parts = splitComment(line.text);
      const step = this.renderer
        .renderInline(markKeywords(parts.step))
        .replace(new RegExp(`${KEYWORD_OPEN}([^${KEYWORD_CLOSE}]*)${KEYWORD_CLOSE}`, 'g'), '<strong class="algorithm-keyword">$1</strong>');
      return {
        depth: line.depth,
        step: this.sanitizer.bypassSecurityTrustHtml(step),
        comment: parts.comment === null ? null : this.sanitizer.bypassSecurityTrustHtml(this.renderer.renderInline(parts.comment)),
      };
    }),
  );
}

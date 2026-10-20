import { ChangeDetectionStrategy, Component, ElementRef, effect, inject, input } from '@angular/core';
import { MarkdownRenderer } from './markdown-renderer';

/**
 * A line of course text, such as a title, a goal or a choice of an MCQ, which may hold
 * maths ($…$), emphasis or code.
 */
@Component({
  selector: 'app-rich-text',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '',
})
export class RichTextComponent {
  readonly text = input.required<string>();

  private readonly host: HTMLElement = inject(ElementRef).nativeElement;
  private readonly renderer = inject(MarkdownRenderer);

  constructor() {
    effect(() => (this.host.innerHTML = this.renderer.renderInline(this.text())));
  }
}

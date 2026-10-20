import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { PdfBlockView } from '../../core/api';
import { LearnFacade } from '../learn.facade';

/** A page of the learner's documents, opened beside the text. */
@Component({
  selector: 'app-pdf-block',
  imports: [TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="media-card pdf" [class.active]="active()" (click)="open()">
      <span class="media-icon" aria-hidden="true">📄</span>
      <span class="media-text">
        <span class="media-action">
          {{ 'block.open' | transloco }}
          @if (pdf().page) {
            · {{ 'block.page' | transloco: { page: pdf().page } }}
          }
        </span>
        <span>{{ pdf().label || pdf().path }}</span>
      </span>
    </button>
  `,
})
export class PdfBlockComponent {
  readonly pdf = input.required<PdfBlockView>();
  private readonly learn = inject(LearnFacade);

  protected readonly active = computed(() => {
    const panel = this.learn.panel();
    return panel?.kind === 'pdf' && panel.path === this.pdf().path && panel.page === this.pdf().page;
  });

  protected open(): void {
    const { label, path, page } = this.pdf();
    this.learn.panel.set({ kind: 'pdf', label, path, page });
  }
}

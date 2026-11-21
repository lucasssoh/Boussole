import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { IconComponent } from '../../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { PdfBlockView } from '../../core/api';
import { LearnFacade } from '../learn.facade';
import { panelKey, sourcePanel } from '../session-documents';

/** A page of the learner's documents, opened beside the text. */
@Component({
  selector: 'app-pdf-block',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="media-card pdf" [class.active]="active()" (click)="open()">
      <span class="media-icon"><app-icon name="document" [size]="26" /></span>
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

  private readonly opened = computed(() => sourcePanel(this.pdf().label, this.pdf().path, this.pdf().page));

  protected readonly active = computed(() => {
    const panel = this.learn.panel();
    return !!panel && panelKey(panel) === panelKey(this.opened());
  });

  protected open(): void {
    this.learn.panel.set(this.opened());
  }
}

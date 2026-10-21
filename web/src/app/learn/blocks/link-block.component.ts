import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { LinkBlockView } from '../../core/api';
import { IconComponent } from '../../shared/icon.component';
import { LearnFacade } from '../learn.facade';

/** A web page cited by the course: documentation, an article, a reference, opened beside the text. */
@Component({
  selector: 'app-link-block',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="media-card" [class.active]="active()" (click)="open()">
      <span class="media-picture web-picture"><app-icon name="globe" [size]="32" /></span>
      <span class="media-text">
        <span class="media-action"><app-icon name="globe" /> {{ 'block.visit' | transloco }} · {{ host() }}</span>
        <span>{{ link().label }}</span>
      </span>
    </button>
  `,
})
export class LinkBlockComponent {
  readonly link = input.required<LinkBlockView>();
  private readonly learn = inject(LearnFacade);

  protected readonly host = computed(() => {
    try {
      return new URL(this.link().url).host.replace(/^www\./, '');
    } catch {
      return this.link().url;
    }
  });
  protected readonly active = computed(() => {
    const panel = this.learn.panel();
    return panel?.kind === 'web' && panel.url === this.link().url;
  });

  protected open(): void {
    const { label, url } = this.link();
    this.learn.panel.set({ kind: 'web', label, url });
  }
}

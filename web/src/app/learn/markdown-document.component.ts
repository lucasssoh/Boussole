import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BoussoleApi } from '../core/boussole-api';
import { LoadingComponent } from '../shared/loading.component';
import { LearnFacade } from './learn.facade';
import { ProseBlockComponent } from './blocks/prose-block.component';

/** A Markdown document cited by a session, rendered beside the lesson. */
@Component({
  selector: 'app-markdown-document',
  imports: [LoadingComponent, ProseBlockComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <app-loading />
    } @else if (error()) {
      <p class="markdown-document-error" role="alert">{{ 'documents.loadFailed' | transloco }}</p>
    } @else {
      <div class="markdown-document">
        <app-prose-block [markdown]="markdown()" [base]="base()" />
      </div>
    }
  `,
})
export class MarkdownDocumentComponent {
  readonly path = input.required<string>();

  private readonly api = inject(BoussoleApi);
  private readonly learn = inject(LearnFacade);
  protected readonly markdown = signal('');
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly base = signal<string | null>(null);

  constructor() {
    effect((onCleanup) => {
      const path = this.path();
      let current = true;
      onCleanup(() => { current = false; });
      this.loading.set(true);
      this.error.set(false);
      this.base.set(this.learn.fileUrl(path.substring(0, path.lastIndexOf('/') + 1)));
      void this.api.fileText(this.learn.fileUrl(path)).then(
        (markdown) => {
          if (current) {
            this.markdown.set(markdown);
            this.loading.set(false);
          }
        },
        () => {
          if (current) {
            this.error.set(true);
            this.loading.set(false);
          }
        },
      );
    });
  }
}

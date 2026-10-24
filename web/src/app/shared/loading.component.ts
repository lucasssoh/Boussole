import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

/**
 * While a page waits for its content: the needle of Boussole turning slowly. It shows only after a
 * short delay, so that a page that loads at once never flickers.
 */
@Component({
  selector: 'app-loading',
  imports: [TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'loading', role: 'status' },
  template: `
    <svg width="56" height="56" viewBox="0 0 64 64" aria-hidden="true">
      <circle cx="32" cy="32" r="27" fill="none" stroke="currentColor" stroke-width="4" opacity="0.18"></circle>
      <g class="loading-needle">
        <path d="M32 10 L38 32 L32 54 L26 32 Z" fill="currentColor" opacity="0.35"></path>
        <path d="M32 10 L38 32 L26 32 Z" fill="currentColor"></path>
      </g>
      <circle cx="32" cy="32" r="3.5" fill="currentColor"></circle>
    </svg>
    <span class="visually-hidden">{{ 'app.loading' | transloco }}</span>
  `,
})
export class LoadingComponent {}

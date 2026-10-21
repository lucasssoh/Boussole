import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { PagePreview } from '../core/api';
import { LearnFacade } from './learn.facade';
import { WebReferences } from './web-references';

/**
 * The video (YouTube or a file of the sources), the document or the web page opened beside the
 * text. A web page shows in place when its site allows it; otherwise a card offers to open it in
 * a tab, since many sites refuse to be shown inside another page.
 */
@Component({
  selector: 'app-side-panel',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'side-panel' },
  template: `
    @if (learn.panel(); as panel) {
      <div class="panel-bar">
        <span class="panel-title">{{ panel.label }}</span>
        @if (newTabUrl(); as url) {
          <a [href]="url" target="_blank" rel="noopener">{{ 'panel.newTab' | transloco }}</a>
        }
        <button type="button" class="quiet" (click)="learn.panel.set(null)">{{ 'panel.close' | transloco }} <app-icon name="close" /></button>
      </div>
      @if (panel.kind === 'web') {
        @if (webPreview(); as preview) {
          @if (preview.embeddable && webFrameUrl(); as url) {
            <iframe class="web" [src]="url" [title]="panel.label" sandbox="allow-scripts allow-same-origin allow-popups allow-forms"
                    referrerpolicy="strict-origin-when-cross-origin"></iframe>
          } @else {
            <div class="panel-card">
              <app-icon name="globe" [size]="40" />
              <strong>{{ preview.title ?? panel.label }}</strong>
              <span class="panel-host">{{ host(panel.url) }}</span>
              @if (references.openedApart() === panel.url) {
                <p>{{ 'panel.webApart' | transloco }}</p>
                <button type="button" class="primary" (click)="openApart(panel.url)">
                  {{ 'panel.backToSite' | transloco }} <app-icon name="arrow-right" />
                </button>
              } @else {
                <p>{{ (preview.reachable ? 'panel.webElsewhere' : 'panel.webUnreachable') | transloco }}</p>
                <button type="button" class="primary" (click)="openApart(panel.url)">
                  {{ 'panel.openSite' | transloco }} <app-icon name="arrow-right" />
                </button>
              }
            </div>
          }
        } @else {
          <p class="panel-card">{{ 'panel.opening' | transloco }}</p>
        }
      } @else if (panel.kind === 'localVideo') {
        <video class="video" [src]="localVideoUrl()" controls autoplay playsinline [title]="panel.label"></video>
      } @else if (frameUrl(); as url) {
        <iframe
          [class]="panel.kind"
          [src]="url"
          [title]="panel.label"
          allow="encrypted-media; picture-in-picture; fullscreen"
          referrerpolicy="strict-origin-when-cross-origin"
        ></iframe>
      }
    }
  `,
})
export class SidePanelComponent {
  protected readonly learn = inject(LearnFacade);
  private readonly sanitizer = inject(DomSanitizer);
  protected readonly references = inject(WebReferences);

  /** What the server learned of the web page open, once it answers. */
  protected readonly webPreview = signal<PagePreview | null>(null);

  protected readonly webFrameUrl = computed<SafeResourceUrl | null>(() => {
    const panel = this.learn.panel();
    return panel?.kind === 'web' ? this.sanitizer.bypassSecurityTrustResourceUrl(panel.url) : null;
  });

  constructor() {
    effect(() => {
      const panel = this.learn.panel();
      untracked(() => this.webPreview.set(null));
      if (panel?.kind === 'web') {
        void this.references.preview(panel.url).then((preview) => {
          if (this.learn.panel() === panel) {
            this.webPreview.set(preview);
          }
        });
      }
    });
  }

  /** In the reference window; a plain tab when the browser refuses the window. */
  protected openApart(url: string): void {
    if (!this.references.openWindow(url)) {
      window.open(url, '_blank', 'noopener');
    }
  }

  protected host(url: string): string {
    try {
      return new URL(url).host.replace(/^www\./, '');
    } catch {
      return url;
    }
  }

  protected readonly newTabUrl = computed(() => {
    const panel = this.learn.panel();
    if (!panel) {
      return null;
    }
    switch (panel.kind) {
      case 'pdf':
        return this.learn.fileUrl(panel.path) + (panel.page ? `#page=${panel.page}` : '');
      case 'localVideo':
        return this.localVideoUrl();
      case 'web':
        return panel.url;
      case 'video':
        return `https://www.youtube.com/watch?v=${panel.youtubeId}${panel.start ? `&t=${panel.start}s` : ''}`;
    }
  });

  /** A video file, from its start and stopping at its end: a media fragment, {@code #t=start,end}. */
  protected readonly localVideoUrl = computed(() => {
    const panel = this.learn.panel();
    if (panel?.kind !== 'localVideo') {
      return '';
    }
    const fragment = panel.start === null && panel.end === null ? '' : `#t=${panel.start ?? 0}${panel.end === null ? '' : ',' + panel.end}`;
    return this.learn.fileUrl(panel.path) + fragment;
  });

  protected readonly frameUrl = computed<SafeResourceUrl | null>(() => {
    const panel = this.learn.panel();
    if (!panel || panel.kind === 'localVideo' || panel.kind === 'web') {
      return null;
    }
    const url = panel.kind === 'pdf'
      ? this.learn.fileUrl(panel.path) + (panel.page ? `#page=${panel.page}` : '')
      : youtubeEmbed(panel.youtubeId, panel.start, panel.end);
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });
}

function youtubeEmbed(id: string, start: number | null, end: number | null): string {
  const parameters = new URLSearchParams({ rel: '0' });
  if (start !== null) {
    parameters.set('start', String(start));
  }
  if (end !== null) {
    parameters.set('end', String(end));
  }
  return `https://www.youtube-nocookie.com/embed/${encodeURIComponent(id)}?${parameters}`;
}

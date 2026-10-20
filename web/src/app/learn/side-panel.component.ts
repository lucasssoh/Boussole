import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { TranslocoPipe } from '@jsverse/transloco';
import { LearnFacade } from './learn.facade';

/** The video or the document opened beside the text. */
@Component({
  selector: 'app-side-panel',
  imports: [TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'side-panel' },
  template: `
    @if (learn.panel(); as panel) {
      <div class="panel-bar">
        <span class="panel-title">{{ panel.label }}</span>
        @if (newTabUrl(); as url) {
          <a [href]="url" target="_blank" rel="noopener">{{ 'panel.newTab' | transloco }}</a>
        }
        <button type="button" class="quiet" (click)="learn.panel.set(null)">{{ 'panel.close' | transloco }} ✕</button>
      </div>
      @if (frameUrl(); as url) {
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

  protected readonly newTabUrl = computed(() => {
    const panel = this.learn.panel();
    if (!panel) {
      return null;
    }
    return panel.kind === 'pdf'
      ? this.learn.fileUrl(panel.path) + (panel.page ? `#page=${panel.page}` : '')
      : `https://www.youtube.com/watch?v=${panel.youtubeId}${panel.start ? `&t=${panel.start}s` : ''}`;
  });

  protected readonly frameUrl = computed<SafeResourceUrl | null>(() => {
    const panel = this.learn.panel();
    if (!panel) {
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

import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { LearnFacade } from './learn.facade';

/** The video (YouTube or a file of the sources) or the document opened beside the text. */
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
      @if (panel.kind === 'localVideo') {
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
    if (!panel || panel.kind === 'localVideo') {
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

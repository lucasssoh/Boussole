import { ChangeDetectionStrategy, Component, DestroyRef, computed, effect, inject, input, linkedSignal, signal, untracked } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { IconComponent, IconName } from '../shared/icon.component';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { PagePreview } from '../core/api';
import { LoadingComponent } from '../shared/loading.component';
import { RichTextComponent } from '../shared/rich-text.component';
import { clock } from './blocks/video-block.component';
import { LearnFacade, SidePanel } from './learn.facade';
import { DocumentPlace, SessionDocument, panelKey } from './session-documents';
import { WebReferences } from './web-references';
import { MarkdownDocumentComponent } from './markdown-document.component';

/**
 * The video (YouTube or a file of the sources), the document or the web page opened beside the
 * text, or the list of everything the session cites. A web page shows in place when its site
 * allows it; otherwise a card offers to open it in a tab, since many sites refuse to be shown
 * inside another page. While something is shown, the arrow goes back to the list and the title
 * unrolls it over the page; Escape folds the list, then closes the panel.
 */
@Component({
  selector: 'app-side-panel',
  imports: [IconComponent, LoadingComponent, MarkdownDocumentComponent, NgTemplateOutlet, RichTextComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'side-panel', '(document:keydown.escape)': 'onEscape()' },
  template: `
    @if (shown(); as panel) {
      @if (panel.kind === 'documents') {
        <div class="panel-bar">
          <app-icon name="document" class="panel-bar-icon" />
          <span class="panel-title">{{ 'documents.title' | transloco }}</span>
          <button type="button" class="quiet" (click)="learn.panel.set(null)">{{ 'panel.close' | transloco }} <app-icon name="close" /></button>
        </div>
        <div class="documents-list">
          <p class="documents-intro">{{ 'documents.intro' | transloco }}</p>
          <ng-container [ngTemplateOutlet]="list" />
        </div>
      } @else {
        <div class="panel-bar">
          @if (documents().length > 0) {
            <button type="button" class="quiet panel-back" [attr.aria-label]="'documents.back' | transloco" [title]="'documents.back' | transloco"
                    (click)="learn.panel.set({ kind: 'documents' })"><app-icon name="arrow-left" /></button>
            <button type="button" class="quiet panel-switch" [attr.aria-expanded]="listOpen()" [title]="'documents.switch' | transloco"
                    (click)="listOpen.set(!listOpen())">
              <span class="panel-title">{{ panel.label }}</span>
              <app-icon [name]="listOpen() ? 'chevron-up' : 'chevron-down'" />
            </button>
          } @else {
            <span class="panel-title">{{ panel.label }}</span>
          }
          @if (newTabUrl(); as url) {
            <a [href]="url" target="_blank" rel="noopener">{{ 'panel.newTab' | transloco }}</a>
          }
          <button type="button" class="quiet" (click)="learn.panel.set(null)">{{ 'panel.close' | transloco }} <app-icon name="close" /></button>
        </div>
        @if (listOpen()) {
          <div class="documents-dropdown" role="region" [attr.aria-label]="'documents.title' | transloco">
            <ng-container [ngTemplateOutlet]="list" />
          </div>
        }
        <div class="panel-body">
          @if (panel.kind === 'web') {
            @if (webPreview(); as preview) {
              @if (preview.embeddable && webFrameUrl(); as url) {
                <iframe class="web" [src]="url" [title]="panel.label" sandbox="allow-scripts allow-same-origin allow-popups allow-forms"
                        referrerpolicy="strict-origin-when-cross-origin" (load)="loaded()"></iframe>
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
            }
          } @else if (panel.kind === 'localVideo') {
            <video class="video" [src]="localVideoUrl()" controls autoplay playsinline [title]="panel.label"
                   (loadeddata)="loaded()" (error)="loaded()"></video>
          } @else if (panel.kind === 'markdown') {
            <app-markdown-document [path]="panel.path" />
          } @else if (frameUrl(); as url) {
            <iframe
              [class]="panel.kind"
              [src]="url"
              [title]="panel.label"
              allow="encrypted-media; picture-in-picture; fullscreen"
              referrerpolicy="strict-origin-when-cross-origin"
              (load)="loaded()"
            ></iframe>
          }
          @if (loading()) {
            <div class="panel-loading"><app-loading /></div>
          }
        </div>
      }
    }

    <ng-template #list>
      @for (document of documents(); track document.key) {
        <section class="document-entry">
          <h3 class="document-title">
            <app-icon [name]="icon(document)" />
            <span class="document-name">{{ document.title }}</span>
            <span class="document-kind">{{ kindOf(document) }}</span>
          </h3>
          <ul class="document-places">
            @for (place of document.places; track $index) {
              <li>
                <button type="button" class="document-place" [class.shown]="isShown(place)" [attr.aria-current]="isShown(place) ? 'true' : null" (click)="choose(place)">
                  @if (where(place); as at) {
                    <span class="place-where">{{ at }}</span>
                  }
                  <span class="place-label"><app-rich-text [text]="place.label || (where(place) ? '' : document.title)" /></span>
                  @if (place.screens.includes(screen())) {
                    <span class="place-here">{{ 'documents.thisStep' | transloco }}</span>
                  }
                </button>
              </li>
            }
          </ul>
        </section>
      }
    </ng-template>
  `,
})
export class SidePanelComponent {
  /** Everything the session cites. */
  readonly documents = input<SessionDocument[]>([]);
  /** The screen shown, by its index in the plan of the session. */
  readonly screen = input(0);

  protected readonly learn = inject(LearnFacade);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly transloco = inject(TranslocoService);
  protected readonly references = inject(WebReferences);

  /**
   * What the panel shows: what is open, and, once closed, what was, while the panel leaves
   * (animate.leave, in the session page).
   */
  protected readonly shown = linkedSignal<SidePanel | null, SidePanel | null>({
    source: this.learn.panel,
    computation: (panel, previous) => panel ?? previous?.value ?? null,
  });

  /** The list unrolled over what is shown. */
  protected readonly listOpen = signal(false);
  /** What the list marks: what is shown, else, on the list, the last thing shown. */
  private readonly shownKey = computed(() => {
    const panel = this.shown();
    const shown = panel?.kind === 'documents' ? this.learn.lastShown() : panel;
    return shown ? panelKey(shown) : null;
  });

  /** What the server learned of the web page open, once it answers. */
  protected readonly webPreview = signal<PagePreview | null>(null);

  protected readonly webFrameUrl = computed<SafeResourceUrl | null>(() => {
    const panel = this.shown();
    return panel?.kind === 'web' ? this.sanitizer.bypassSecurityTrustResourceUrl(panel.url) : null;
  });

  /**
   * The page the panel loads, without the page of a document: going to another page of the same
   * document loads nothing, so shows no wait.
   */
  private readonly pageKey = computed(() => {
    const panel = this.shown();
    switch (panel?.kind) {
      case undefined:
      case 'documents':
        return null;
      case 'markdown':
        return null;
      case 'pdf':
      case 'localVideo':
        return `file:${panel.path}`;
      case 'web':
        return `web:${panel.url}`;
      case 'video':
        return `video:${panel.youtubeId}@${panel.start ?? ''}-${panel.end ?? ''}`;
    }
  });
  private readonly loadedKey = signal<string | null>(null);
  /** The document, the site or the video is on its way: a site that refuses frames has nothing to wait for. */
  protected readonly loading = computed(() => {
    const key = this.pageKey();
    if (key === null) {
      return false;
    }
    if (this.shown()?.kind === 'web') {
      const preview = this.webPreview();
      return preview === null || (preview.embeddable && this.loadedKey() !== key);
    }
    return this.loadedKey() !== key;
  });

  constructor() {
    effect(() => {
      this.shown();
      untracked(() => this.listOpen.set(false));
    });
    // A page that never says it has loaded does not hide the panel for good.
    let giveUp: ReturnType<typeof setTimeout> | undefined;
    inject(DestroyRef).onDestroy(() => clearTimeout(giveUp));
    effect(() => {
      const key = this.pageKey();
      clearTimeout(giveUp);
      if (key !== null) {
        giveUp = setTimeout(() => this.loadedKey.set(key), LOADING_LIMIT_MS);
      }
    });
    effect(() => {
      // The web pages of the list are looked at now, so that a click knows where they open.
      for (const document of this.documents()) {
        const panel = document.places[0].panel;
        if (panel.kind === 'web') {
          void this.references.preview(panel.url);
        }
      }
    });
    effect(() => {
      const panel = this.shown();
      untracked(() => this.webPreview.set(null));
      if (panel?.kind === 'web') {
        void this.references.preview(panel.url).then((preview) => {
          if (this.shown() === panel) {
            this.webPreview.set(preview);
          }
        });
      }
    });
  }

  protected loaded(): void {
    this.loadedKey.set(this.pageKey());
  }

  protected onEscape(): void {
    if (this.listOpen()) {
      this.listOpen.set(false);
    } else if (this.learn.panel()) {
      this.learn.panel.set(null);
    }
  }

  protected choose(place: DocumentPlace): void {
    const panel = place.panel;
    if (panel.kind === 'web') {
      this.references.open(panel.label || panel.url, panel.url);
    } else {
      this.learn.panel.set(panel);
    }
    this.listOpen.set(false);
  }

  protected isShown(place: DocumentPlace): boolean {
    return panelKey(place.panel) === this.shownKey();
  }

  /** Where in the document: its page, or the moment of a video. */
  protected where(place: DocumentPlace): string {
    if (place.page !== null) {
      return this.transloco.translate('block.page', { page: place.page });
    }
    return place.start !== null ? clock(place.start) : '';
  }

  protected icon(document: SessionDocument): IconName {
    return document.kind === 'web' ? 'globe' : document.kind === 'pdf' || document.kind === 'markdown' ? 'document' : 'play';
  }

  /** The kind of file, the site of a web page, or YouTube. */
  protected kindOf(document: SessionDocument): string {
    const panel = document.places[0].panel;
    switch (panel.kind) {
      case 'web':
        return this.host(panel.url);
      case 'video':
        return 'YouTube';
      default: {
        const dot = document.title.lastIndexOf('.');
        return dot > 0 ? document.title.substring(dot + 1).toUpperCase() : '';
      }
    }
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
    const panel = this.shown();
    if (!panel) {
      return null;
    }
    switch (panel.kind) {
      case 'documents':
      case 'markdown':
        return null;
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
    const panel = this.shown();
    if (panel?.kind !== 'localVideo') {
      return '';
    }
    const fragment = panel.start === null && panel.end === null ? '' : `#t=${panel.start ?? 0}${panel.end === null ? '' : ',' + panel.end}`;
    return this.learn.fileUrl(panel.path) + fragment;
  });

  protected readonly frameUrl = computed<SafeResourceUrl | null>(() => {
    const panel = this.shown();
    if (!panel || panel.kind === 'documents' || panel.kind === 'markdown' || panel.kind === 'localVideo' || panel.kind === 'web') {
      return null;
    }
    const url = panel.kind === 'pdf'
      ? this.learn.fileUrl(panel.path) + (panel.page ? `#page=${panel.page}` : '')
      : youtubeEmbed(panel.youtubeId, panel.start, panel.end);
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });
}

/** How long the panel waits for a page before showing it anyway. */
const LOADING_LIMIT_MS = 15000;

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

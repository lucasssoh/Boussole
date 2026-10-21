import { Injectable, inject, signal } from '@angular/core';
import { PagePreview } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { LearnFacade } from './learn.facade';

const UNREACHABLE: PagePreview = { reachable: false, embeddable: false, title: null };
/** One window for every reference that cannot show beside the text, reused rather than piling up tabs. */
const WINDOW_NAME = 'boussole-reference';

/**
 * The web pages a session cites. Each is looked at as soon as its step shows, so that a click
 * already knows whether the page shows beside the text or, when its site refuses frames, opens
 * at once in a window of its own, placed on the right: a browser only lets a click open a window.
 */
@Injectable({ providedIn: 'root' })
export class WebReferences {
  private readonly api = inject(BoussoleApi);
  private readonly learn = inject(LearnFacade);
  private readonly previews = new Map<string, Promise<PagePreview>>();
  private readonly known = new Map<string, PagePreview>();

  /** The page last opened in the reference window. */
  readonly openedApart = signal<string | null>(null);

  preview(url: string): Promise<PagePreview> {
    let preview = this.previews.get(url);
    if (!preview) {
      preview = this.api.previewPage(url).catch(() => UNREACHABLE);
      preview.then((found) => this.known.set(url, found));
      this.previews.set(url, preview);
    }
    return preview;
  }

  /** From a click: beside the text, and also in the reference window when the site is known to refuse frames. */
  open(label: string, url: string): void {
    const known = this.known.get(url);
    if (known && known.reachable && !known.embeddable) {
      this.openWindow(url);
    }
    this.learn.panel.set({ kind: 'web', label, url });
  }

  /** From a click too. Says whether the browser let the window open. */
  openWindow(url: string): boolean {
    const width = Math.round(Math.min(screen.availWidth / 2, 1100));
    const left = Math.max(0, screen.availWidth - width);
    const opened = window.open('about:blank', WINDOW_NAME, `popup=yes,width=${width},height=${screen.availHeight},left=${left},top=0`);
    if (!opened) {
      return false;
    }
    // The page opened must not be able to steer Boussole's own tab.
    opened.opener = null;
    opened.location.href = url;
    opened.focus();
    this.openedApart.set(url);
    return true;
  }
}

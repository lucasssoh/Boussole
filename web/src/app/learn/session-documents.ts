import { BlockView } from '../core/api';
import { InlineReference } from '../shared/markdown-renderer';
import { SidePanel } from './learn.facade';
import { SessionPlan } from './session-screens';

/** A place of a document the session cites: a page, a moment of a video, or the web page itself. */
export interface DocumentPlace {
  /** The label the course gives it, as written; empty when the course gives none. */
  label: string;
  page: number | null;
  start: number | null;
  /** What opens beside the text. */
  panel: Exclude<SidePanel, { kind: 'documents' }>;
  /** The screens that cite it, by their index in the plan. */
  screens: number[];
}

/** A document of the sources, a web page or a video the session cites, with each place of it cited. */
export interface SessionDocument {
  key: string;
  kind: 'pdf' | 'web' | 'video' | 'localVideo';
  /** The file name, or the label of the first citation for a web page or a YouTube video. */
  title: string;
  places: DocumentPlace[];
}

/** How the documents are read from the session: the :pdf and :link of its prose, and a file of :pdf to a path. */
export interface DocumentReading {
  references(markdown: string): InlineReference[];
  sourcePath(file: string): string;
}

type Opened = DocumentPlace['panel'];

/**
 * Everything a session cites, gathered in one list: the documents of its blocks and of its
 * sentences, in the order they come, then those its front matter names and no screen cites.
 * The same page cited twice is one place; the pages of a document are in order.
 */
export function sessionDocuments(
  plan: SessionPlan,
  sources: { path: string; page: number | null }[],
  reading: DocumentReading,
): SessionDocument[] {
  const documents = new Map<string, SessionDocument>();
  const cite = (panel: Opened, screen: number | null): void => {
    const key = documentKey(panel);
    let document = documents.get(key);
    if (!document) {
      document = { key, kind: panel.kind, title: documentTitle(panel), places: [] };
      documents.set(key, document);
    }
    const place = document.places.find((found) => panelKey(found.panel) === panelKey(panel));
    if (place) {
      if (!place.label && panel.label) {
        place.label = panel.label;
        place.panel = panel;
      }
      if (screen !== null && !place.screens.includes(screen)) {
        place.screens.push(screen);
      }
      return;
    }
    document.places.push({
      label: panel.label,
      page: panel.kind === 'pdf' ? panel.page : null,
      start: panel.kind === 'video' || panel.kind === 'localVideo' ? panel.start : null,
      panel,
      screens: screen === null ? [] : [screen],
    });
  };
  plan.screens.forEach((screen, index) => walk(screen.blocks, reading, (panel) => cite(panel, index)));
  for (const source of sources) {
    cite({ kind: 'pdf', label: '', path: source.path, page: source.page }, null);
  }
  const listed = [...documents.values()];
  for (const document of listed) {
    if (document.kind === 'pdf') {
      document.places.sort((one, other) => (one.page ?? 0) - (other.page ?? 0));
    }
    if (document.kind !== 'pdf' && document.kind !== 'localVideo') {
      document.title = document.places.find((place) => place.label)?.label || document.title;
    }
  }
  return listed;
}

/** The same thing opened beside the text: a page of a document, a moment of a video, a web page. */
export function panelKey(panel: SidePanel): string {
  switch (panel.kind) {
    case 'documents':
      return 'documents';
    case 'pdf':
      return `pdf:${panel.path}#${panel.page ?? ''}`;
    case 'web':
      return `web:${panel.url}`;
    case 'video':
      return `video:${panel.youtubeId}@${panel.start ?? ''}`;
    case 'localVideo':
      return `file:${panel.path}@${panel.start ?? ''}`;
  }
}

/** The document a panel shows, whatever its page or moment. */
export function documentKey(panel: Opened): string {
  switch (panel.kind) {
    case 'pdf':
    case 'localVideo':
      return `file:${panel.path}`;
    case 'web':
      return `web:${panel.url}`;
    case 'video':
      return `video:${panel.youtubeId}`;
  }
}

function documentTitle(panel: Opened): string {
  switch (panel.kind) {
    case 'pdf':
    case 'localVideo':
      return panel.path.substring(panel.path.lastIndexOf('/') + 1);
    case 'web':
      return panel.url;
    case 'video':
      return panel.label;
  }
}

function walk(blocks: BlockView[], reading: DocumentReading, cite: (panel: Opened) => void): void {
  for (const block of blocks) {
    switch (block.type) {
      case 'prose':
        for (const reference of reading.references(block.markdown)) {
          if (reference.name === 'link') {
            cite({ kind: 'web', label: reference.text, url: reference.attributes['url'] ?? '' });
          } else {
            const page = Number(reference.attributes['page']);
            cite({ kind: 'pdf', label: reference.text, path: reading.sourcePath(reference.attributes['src'] ?? ''),
              page: Number.isInteger(page) && page > 0 ? page : null });
          }
        }
        break;
      case 'pdf':
        cite({ kind: 'pdf', label: block.label, path: block.path, page: block.page });
        break;
      case 'link':
        cite({ kind: 'web', label: block.label, url: block.url });
        break;
      case 'video':
        cite(block.path !== null
          ? { kind: 'localVideo', label: block.label, path: block.path, start: block.start, end: block.end }
          : { kind: 'video', label: block.label, youtubeId: block.youtubeId ?? '', start: block.start, end: block.end });
        break;
      case 'example':
      case 'recall':
      case 'callout':
        walk(block.blocks, reading, cite);
        break;
      case 'mcq':
      case 'exercise':
        walk(block.statement, reading, cite);
        break;
      case 'graph':
      case 'missingRef':
        break;
    }
  }
}

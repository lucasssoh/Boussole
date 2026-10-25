import { TestBed } from '@angular/core/testing';
import { BlockView, SessionPart } from '../core/api';
import { MarkdownRenderer } from '../shared/markdown-renderer';
import { panelKey, sessionDocuments } from './session-documents';
import { sessionPlan } from './session-screens';

const long = 'x'.repeat(400);
const prose = (markdown: string): BlockView => ({ type: 'prose', markdown: `${markdown} ${long}` });
const pdf = (label: string, path: string, page: number | null): BlockView => ({ type: 'pdf', label, path, page });
const part = (blocks: BlockView[]): SessionPart => ({ kind: 'notion', id: null, title: null, blocks });

describe('sessionDocuments', () => {
  let reading: { references: MarkdownRenderer['references']; sourcePath: (file: string) => string };

  beforeEach(() => {
    const renderer = TestBed.inject(MarkdownRenderer);
    reading = { references: (markdown) => renderer.references(markdown), sourcePath: (file) => `sources/${file}` };
  });

  it('gathers the pages of a document cited in blocks and in sentences, in page order, with the screens citing them', () => {
    const plan = sessionPlan([part([
      prose('Trois écritures (:pdf[transparent 2]{src=cours.pdf page=2}).'),
      { type: 'example', label: 'Exemple', blocks: [pdf('Les trois formes', 'sources/cours.pdf', 2)] },
      pdf('Le paradigme', 'sources/cours.pdf', 1),
    ])]);
    const [document] = sessionDocuments(plan, [], reading);
    expect(document.title).toBe('cours.pdf');
    expect(document.places.map((place) => [place.page, place.label, place.screens])).toEqual([
      [1, 'Le paradigme', [3]],
      [2, 'transparent 2', [1, 2]],
    ]);
  });

  it('adds the documents of the front matter no screen cites, and leaves out the :pdf written in code', () => {
    const plan = sessionPlan([part([prose('`:pdf[pas un renvoi]{src=code.pdf}` et :link[la doc]{url=https://example.org/doc}')])]);
    const documents = sessionDocuments(plan, [{ path: 'sources/td1.pdf', page: 3 }], reading);
    expect(documents.map((document) => [document.kind, document.title])).toEqual([
      ['web', 'la doc'],
      ['pdf', 'td1.pdf'],
    ]);
    expect(documents[1].places[0].screens).toEqual([]);
  });

  it('knows a page by its document and its page, whatever its label', () => {
    expect(panelKey({ kind: 'pdf', label: 'un', path: 'sources/cours.pdf', page: 2 }))
      .toBe(panelKey({ kind: 'pdf', label: 'autre', path: 'sources/cours.pdf', page: 2 }));
  });
});

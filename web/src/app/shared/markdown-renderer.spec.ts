import { TestBed } from '@angular/core/testing';
import { MarkdownRenderer } from './markdown-renderer';

describe('MarkdownRenderer', () => {
  const context = { base: '/api/subjects/oc/files/cursus/03-dualite/' };
  let renderer: MarkdownRenderer;

  beforeEach(() => (renderer = TestBed.inject(MarkdownRenderer)));

  it('renders maths with KaTeX', () => {
    expect(renderer.render('Soit $u = c^I$.', context)).toContain('class="katex"');
  });

  it('turns a :term into a span the lexicon fills', () => {
    const html = renderer.render(':term[dualité faible]{id=dualite-faible} borne', context);
    expect(html).toContain('class="term"');
    expect(html).toContain('data-term-id="dualite-faible"');
  });

  it('turns a :pdf into a button naming the file and the page', () => {
    const html = renderer.render('Voir :pdf[le transparent]{src=oc08.pdf page=4}.', context);
    expect(html).toContain('data-pdf-src="oc08.pdf"');
    expect(html).toContain('data-pdf-page="4"');
  });

  it('marks the gaps of a fill exercise', () => {
    expect(renderer.render('La contrainte [[1]] est lâche.', context)).toContain('<span class="gap" data-gap="1"></span>');
  });

  it('resolves an image against the folder of the session file', () => {
    expect(renderer.render('![figure](../assets/coupe.svg)', context)).toContain('src="/api/subjects/oc/files/cursus/assets/coupe.svg"');
  });

  it('shows raw HTML as text', () => {
    expect(renderer.render('<script>alert(1)</script>', context)).not.toContain('<script>');
  });

  it('keeps a Mermaid diagram for Mermaid to draw', () => {
    expect(renderer.render('```mermaid\ngraph TD; A-->B\n```', context)).toContain('<pre class="mermaid">');
  });
});

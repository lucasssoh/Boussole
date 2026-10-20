/** Draws the Mermaid diagrams of an element. Mermaid is large: it is loaded the first time a diagram shows. */
export async function drawMermaidDiagrams(root: HTMLElement): Promise<void> {
  const diagrams = Array.from(root.querySelectorAll<HTMLElement>('pre.mermaid:not([data-processed])'));
  if (diagrams.length === 0) {
    return;
  }
  const mermaid = (await import('mermaid')).default;
  const dark = document.documentElement.getAttribute('data-theme') === 'dark'
    || (!document.documentElement.hasAttribute('data-theme') && matchMedia('(prefers-color-scheme: dark)').matches);
  // Mermaid sizes each box from the text it measures: with the page font not loaded yet, the
  // boxes come out too narrow and the labels are cut.
  await document.fonts.ready;
  const font = getComputedStyle(root).fontFamily;
  mermaid.initialize({
    startOnLoad: false,
    theme: dark ? 'dark' : 'neutral',
    securityLevel: 'strict',
    fontFamily: font,
    themeVariables: { fontFamily: font },
    htmlLabels: false,
    flowchart: { htmlLabels: false, wrappingWidth: 160 },
  });
  await mermaid.run({ nodes: diagrams });
}

/**
 * Where the picture of each achievement comes from: a photo of Wikimedia Commons, cut out and turned
 * grey (public/achievements/CREDITS.md says the same, with links). An empty author is unknown.
 */
export const ACHIEVEMENT_PHOTOS: Readonly<Record<string, { author: string; licence: string }>> = {
  'alone-at-the-helm': { author: 'Gko6166', licence: 'CC BY-SA 3.0' },
  'ariadnes-thread': { author: 'Douglas Muth', licence: 'CC BY-SA 2.0' },
  'at-the-first-stroke': { author: 'Wacky1976', licence: 'CC BY-SA 4.0' },
  'compass-in-hand': { author: 'Valentin Antonucci', licence: 'CC0' },
  'drop-by-drop': { author: 'Skitterphoto', licence: 'CC0' },
  'from-stream-to-ocean': { author: 'Katsushika Hokusai', licence: 'public domain' },
  'ithaca': { author: '', licence: 'CC BY-SA 3.0' },
  'nothing-escapes-me': { author: 'MichalPL', licence: 'CC BY-SA 4.0' },
  'on-the-shoulders-of-giants': { author: 'Karrota', licence: 'CC BY-SA 3.0' },
  'terra-incognita': { author: '', licence: 'public domain' },
  'the-first-step': { author: 'Neil A. Armstrong, NASA', licence: 'public domain' },
  'the-golden-fleece': { author: 'ArchaiOptix', licence: 'CC BY-SA 4.0' },
  'the-honest-man': { author: 'Lisafern', licence: 'CC0' },
  'the-madeleine': { author: 'Bernard Leprêtre', licence: 'CC BY-SA 2.5' },
  'the-path-is-made-by-walking': { author: 'Brian Green', licence: 'CC BY-SA 2.0' },
  'the-right-word': { author: 'Bhullargraphic', licence: 'CC BY-SA 4.0' },
  'trial-by-fire': { author: 'Jygle', licence: 'CC BY-SA 4.0' },
  'twenty-times-on-the-loom': { author: 'Selwyn Wright', licence: 'CC BY 4.0' },
  'without-a-blot': { author: 'Enrique Íñiguez Rodríguez (Qoan)', licence: 'CC BY-SA 4.0' },
};

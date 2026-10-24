/**
 * The colour of a chapter, by its place in the track: the chapter wears it on the map, its
 * sessions carry it, so that the learner always knows which chapter they are in. Each keeps 3:1
 * against the light ground as a mark and a line; neighbours differ in hue and lightness.
 */
const PALETTE = ['#1864ab', '#c2410c', '#862e9c', '#0c8599', '#a61e4d', '#2b8a3e', '#5f3dc4', '#b35c00', '#c2255c', '#0b7a5f'];

export function chapterColor(index: number): string {
  return PALETTE[((index % PALETTE.length) + PALETTE.length) % PALETTE.length];
}

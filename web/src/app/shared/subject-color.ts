/**
 * The colour of a subject, the same every time: picked from a palette by its ID. Each colour keeps
 * white text readable (4.5:1) and is told apart from its neighbours by lightness as well as hue.
 */
const PALETTE = ['#a61e4d', '#1864ab', '#c2410c', '#5c7cfa', '#0b7a5f', '#2b8a3e', '#862e9c', '#6741d9'];

/** The tracks built into Boussole (their IDs start with boussole-) wear the colour of Boussole itself. */
const BUILT_IN = 'boussole-';
const BRAND = '#0b7a5f';

export function subjectColor(id: string): string {
  if (id.startsWith(BUILT_IN)) {
    return BRAND;
  }
  let hash = 0;
  for (const character of id) {
    hash = (hash * 31 + character.charCodeAt(0)) | 0;
  }
  return PALETTE[Math.abs(hash) % PALETTE.length];
}

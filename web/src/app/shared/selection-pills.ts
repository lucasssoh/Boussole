/** A box on the page, in pixels, its edges as `getClientRects()` gives them. */
export interface Box {
  left: number;
  right: number;
  top: number;
  bottom: number;
}

/** A rounded rectangle drawn under one line of the selected text. */
export interface Pill {
  x: number;
  y: number;
  width: number;
  height: number;
  radius: number;
}

/** Room left around the words, so that a pill does not touch the first and last letters. */
const SIDE = 6;
const ABOVE = 1;

/**
 * The lines of a selection: the boxes of the words on one visual line (a word in bold, a formula
 * and the text around them are separate boxes) become one, when their middles share a line and
 * they touch or nearly. Two columns of a table on the same line stay apart.
 */
export function selectionLines(boxes: readonly Box[], gap = 14): Box[] {
  const rows: { top: number; bottom: number; boxes: Box[] }[] = [];
  const shown = boxes
    .filter((box) => box.right - box.left > 0.5 && box.bottom - box.top > 2)
    .sort((a, b) => a.top - b.top);
  for (const box of shown) {
    const middle = (box.top + box.bottom) / 2;
    const row = rows.find((other) => middle > other.top && middle < other.bottom);
    if (row) {
      row.boxes.push(box);
    } else {
      rows.push({ top: box.top, bottom: box.bottom, boxes: [box] });
    }
  }
  const lines: Box[] = [];
  for (const row of rows) {
    let line: Box | null = null;
    for (const box of [...row.boxes].sort((a, b) => a.left - b.left)) {
      if (line && box.left - line.right < gap) {
        line.right = Math.max(line.right, box.right);
        line.top = Math.min(line.top, box.top);
        line.bottom = Math.max(line.bottom, box.bottom);
      } else {
        line = { ...box };
        lines.push(line);
      }
    }
  }
  return lines.sort((a, b) => a.top - b.top || a.left - b.left);
}

/** The part of a box inside another (a table that scrolls, a frame), or null if none. */
export function clipBox(box: Box, within: Box | null): Box | null {
  if (!within) {
    return box;
  }
  const clipped = {
    left: Math.max(box.left, within.left),
    right: Math.min(box.right, within.right),
    top: Math.max(box.top, within.top),
    bottom: Math.min(box.bottom, within.bottom),
  };
  return clipped.right > clipped.left && clipped.bottom > clipped.top ? clipped : null;
}

/** The pill of a line, its ends fully round. */
export function pillOf(line: Box): Pill {
  const height = line.bottom - line.top + 2 * ABOVE;
  return {
    x: line.left - SIDE,
    y: line.top - ABOVE,
    width: line.right - line.left + 2 * SIDE,
    height,
    radius: height / 2,
  };
}

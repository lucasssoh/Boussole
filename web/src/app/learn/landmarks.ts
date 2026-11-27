import { Injectable, computed, signal } from '@angular/core';
import { BlockView } from '../core/api';
import { storeValue, storedText } from '../core/local-preferences';

/** What the learner keeps in view beside the steps of a track, from any session of it. */
export interface PinnedLandmark {
  key: string;
  title: string;
  blocks: BlockView[];
}

/** A piece of a text that can be kept in view on its own: a formula set apart, or a table. */
export interface PinnablePiece {
  kind: 'math' | 'table';
  markdown: string;
}

/**
 * The landmarks the learner pinned, by track, kept in this browser: they show beside the steps of
 * every session of the track until the learner takes them away.
 */
@Injectable({ providedIn: 'root' })
export class PinnedLandmarks {
  private readonly subject = signal<string | null>(null);
  private readonly pinnedBySubject = signal<Record<string, PinnedLandmark[]>>({});

  readonly pinned = computed(() => {
    const subject = this.subject();
    return subject ? (this.pinnedBySubject()[subject] ?? []) : [];
  });

  /** The track whose landmarks are read and changed. */
  open(subject: string): void {
    this.subject.set(subject);
    if (!(subject in this.pinnedBySubject())) {
      this.pinnedBySubject.update((all) => ({ ...all, [subject]: readPinned(storedText(storageKey(subject))) }));
    }
  }

  isPinned(key: string): boolean {
    return this.pinned().some((landmark) => landmark.key === key);
  }

  toggle(title: string, blocks: BlockView[]): void {
    const key = landmarkKey(blocks);
    this.save(this.isPinned(key) ? this.pinned().filter((landmark) => landmark.key !== key) : [...this.pinned(), { key, title, blocks }]);
  }

  unpin(key: string): void {
    this.save(this.pinned().filter((landmark) => landmark.key !== key));
  }

  private save(pinned: PinnedLandmark[]): void {
    const subject = this.subject();
    if (!subject) {
      return;
    }
    this.pinnedBySubject.update((all) => ({ ...all, [subject]: pinned }));
    storeValue(storageKey(subject), JSON.stringify(pinned));
  }
}

function storageKey(subject: string): string {
  return `landmarks.${subject}`;
}

function readPinned(text: string): PinnedLandmark[] {
  try {
    const value = JSON.parse(text) as unknown;
    return Array.isArray(value)
      ? value.filter((item): item is PinnedLandmark => typeof item?.key === 'string' && typeof item?.title === 'string' && Array.isArray(item?.blocks))
      : [];
  } catch {
    return [];
  }
}

/** The same content pinned twice is one landmark: its key is its content. */
export function landmarkKey(blocks: BlockView[]): string {
  const text = JSON.stringify(blocks);
  let hash = 0;
  for (let index = 0; index < text.length; index++) {
    hash = (Math.imul(31, hash) + text.charCodeAt(index)) | 0;
  }
  return `${(hash >>> 0).toString(36)}-${text.length}`;
}

/**
 * The formulas set apart ({@code $$…$$}) and the tables of a Markdown text, in reading order and
 * each kind apart: the n-th formula drawn is the n-th of the list. Code fences are skipped.
 */
export function pinnablePieces(markdown: string): PinnablePiece[] {
  const pieces: PinnablePiece[] = [];
  const lines = markdown.split('\n');
  let fence: string | null = null;
  for (let index = 0; index < lines.length; index++) {
    const line = lines[index];
    const trimmed = line.trim();
    const opening = /^(`{3,}|~{3,})/.exec(trimmed);
    if (fence !== null) {
      if (trimmed.startsWith(fence)) {
        fence = null;
      }
      continue;
    }
    if (opening) {
      fence = opening[1];
      continue;
    }
    if (trimmed.startsWith('$$')) {
      const block = [line];
      const closedOnSameLine = trimmed.length > 2 && trimmed.endsWith('$$') && trimmed !== '$$';
      while (!closedOnSameLine && index + 1 < lines.length) {
        index++;
        block.push(lines[index]);
        if (lines[index].trim().endsWith('$$')) {
          break;
        }
      }
      pieces.push({ kind: 'math', markdown: block.join('\n') });
      continue;
    }
    if (trimmed.startsWith('|') && index + 1 < lines.length && /^\s*\|?\s*:?-{3,}/.test(lines[index + 1])) {
      const block = [line];
      while (index + 1 < lines.length && lines[index + 1].trim().startsWith('|')) {
        index++;
        block.push(lines[index]);
      }
      pieces.push({ kind: 'table', markdown: block.join('\n') });
    }
  }
  return pieces;
}

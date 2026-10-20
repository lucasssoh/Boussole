import { WritableSignal } from '@angular/core';
import { CheckedItem, FillGapInput } from '../../core/api';
import { iconSvg } from '../../shared/icon.component';

/**
 * The fields of a fill exercise, placed where its statement has [[N]]: a drop-down list for a
 * choice gap, a short field otherwise. Fields are plain DOM elements, since they sit inside
 * rendered Markdown; what the learner gives is kept in {@link values}.
 */
export class FillGaps {
  private marks: CheckedItem[] = [];
  private locked = false;
  private expected: Readonly<Record<string, string>> = {};

  constructor(
    private readonly gaps: FillGapInput[],
    readonly values: WritableSignal<Readonly<Record<number, string>>>,
    private readonly onEnter: () => void,
  ) {}

  /** Replaces the gap markers of rendered Markdown by fields, keeping what was already given. */
  mountIn(root: HTMLElement): void {
    for (const holder of Array.from(root.querySelectorAll<HTMLElement>('span.gap'))) {
      const gap = this.gaps.find((candidate) => candidate.number === Number(holder.dataset['gap']));
      if (gap) {
        holder.replaceChildren(this.field(gap));
        if (gap.unit) {
          holder.append(Object.assign(document.createElement('span'), { className: 'unit', textContent: ` ${gap.unit}` }));
        }
      }
    }
    this.paint(root);
    this.lock(root, this.locked);
  }

  /** Shows which gaps are right, and once the solution is open, what each expected. */
  show(root: HTMLElement, marks: CheckedItem[], expected: Readonly<Record<string, string>>): void {
    this.marks = marks;
    this.expected = expected;
    this.paint(root);
  }

  /** Once answered, the fields can no longer change. */
  lock(root: HTMLElement, locked: boolean): void {
    this.locked = locked;
    root.querySelectorAll<HTMLInputElement | HTMLSelectElement>('span.gap input, span.gap select').forEach((field) => (field.disabled = locked));
  }

  complete(): boolean {
    return this.gaps.every((gap) => (this.values()[gap.number] ?? '').trim() !== '');
  }

  private field(gap: FillGapInput): HTMLInputElement | HTMLSelectElement {
    const value = this.values()[gap.number] ?? '';
    const label = `[[${gap.number}]]`;
    if (gap.kind === 'choice') {
      const select = document.createElement('select');
      select.append(new Option('…', ''), ...(gap.options ?? []).map((option) => new Option(option, option)));
      select.value = value;
      select.ariaLabel = label;
      select.addEventListener('change', () => this.set(gap.number, select.value));
      return select;
    }
    const field = document.createElement('input');
    field.type = 'text';
    field.value = value;
    field.ariaLabel = label;
    field.autocomplete = 'off';
    field.spellcheck = false;
    if (gap.kind === 'number') {
      field.inputMode = 'decimal';
      field.size = 6;
    }
    field.addEventListener('input', () => this.set(gap.number, field.value));
    field.addEventListener('keydown', (event) => event.key === 'Enter' && this.onEnter());
    return field;
  }

  private set(number: number, value: string): void {
    this.values.update((values) => ({ ...values, [number]: value }));
  }

  private paint(root: HTMLElement): void {
    for (const holder of Array.from(root.querySelectorAll<HTMLElement>('span.gap'))) {
      const key = holder.dataset['gap'] ?? '';
      const mark = this.marks.find((item) => item.key === key);
      holder.classList.toggle('correct', mark?.correct === true);
      holder.classList.toggle('wrong', mark?.correct === false);
      holder.querySelector('.expected')?.remove();
      if (this.expected[key] !== undefined && mark?.correct !== true) {
        const expected = document.createElement('span');
        expected.className = 'expected';
        expected.innerHTML = iconSvg('arrow-right', 14);
        expected.append(` ${this.expected[key]}`);
        holder.append(expected);
      }
    }
  }
}

import { ApplicationRef, ComponentRef, WritableSignal, createComponent } from '@angular/core';
import { CheckedItem, FillGapInput } from '../../core/api';
import { ChoiceMenuComponent } from '../../shared/choice-menu.component';
import { iconSvg } from '../../shared/icon.component';

/**
 * The fields of a fill exercise, placed where its statement has [[N]]: a drop-down list for a
 * choice gap, a short field otherwise. They sit inside rendered Markdown, so a short field is a
 * plain DOM element and a list a component created by hand; what the learner gives is kept in
 * {@link values}.
 */
export class FillGaps {
  private marks: CheckedItem[] = [];
  private locked = false;
  private expected: Readonly<Record<string, string>> = {};
  private menus: ComponentRef<ChoiceMenuComponent>[] = [];

  constructor(
    private readonly gaps: FillGapInput[],
    readonly values: WritableSignal<Readonly<Record<number, string>>>,
    private readonly onEnter: () => void,
    private readonly app: ApplicationRef,
  ) {}

  /** Replaces the gap markers of rendered Markdown by fields, keeping what was already given. */
  mountIn(root: HTMLElement): void {
    this.destroy();
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
    root.querySelectorAll<HTMLInputElement>('span.gap input').forEach((field) => (field.disabled = locked));
    this.menus.forEach((menu) => menu.setInput('disabled', locked));
  }

  /** The lists are attached to the application: they go with the statement they were put in. */
  destroy(): void {
    this.menus.forEach((menu) => {
      this.app.detachView(menu.hostView);
      menu.destroy();
    });
    this.menus = [];
  }

  complete(): boolean {
    return this.gaps.every((gap) => (this.values()[gap.number] ?? '').trim() !== '');
  }

  private field(gap: FillGapInput): HTMLElement {
    const value = this.values()[gap.number] ?? '';
    const label = `[[${gap.number}]]`;
    if (gap.kind === 'choice') {
      const menu = createComponent(ChoiceMenuComponent, { environmentInjector: this.app.injector });
      menu.setInput('options', gap.options ?? []);
      menu.setInput('value', value);
      menu.setInput('label', label);
      menu.instance.picked.subscribe((option) => {
        menu.setInput('value', option);
        this.set(gap.number, option);
      });
      this.app.attachView(menu.hostView);
      this.menus.push(menu);
      return menu.location.nativeElement as HTMLElement;
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

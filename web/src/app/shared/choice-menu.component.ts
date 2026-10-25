import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { IconComponent } from './icon.component';
import { RichTextComponent } from './rich-text.component';

let nextId = 0;

/**
 * A drop-down list drawn by the interface rather than by the browser, whose open list follows
 * neither the theme nor the font: a button in relief, and under it the choices. Used from the keyboard as a select is: arrows, Home and End, Enter or Space, Escape.
 */
@Component({
  selector: 'app-choice-menu',
  imports: [IconComponent, RichTextComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'choice-menu', '[class.open]': 'open()', '(focusout)': 'leave($event)' },
  template: `
    <button
      type="button"
      class="choice-trigger"
      role="combobox"
      aria-haspopup="listbox"
      [attr.aria-label]="label()"
      [attr.aria-expanded]="open()"
      [attr.aria-controls]="id + '-list'"
      [attr.aria-activedescendant]="open() && active() >= 0 ? id + '-' + active() : null"
      [class.empty]="!value()"
      [disabled]="disabled()"
      (click)="toggle()"
      (keydown)="key($event)"
    >
      @if (value(); as chosen) {
        <app-rich-text [text]="chosen" />
      } @else {
        <span>{{ placeholder() }}</span>
      }
      <app-icon name="chevron-down" [size]="16" />
    </button>
    @if (open()) {
      <ul class="choice-list" role="listbox" [id]="id + '-list'" [attr.aria-label]="label()">
        @for (option of options(); track option; let index = $index) {
          <li
            role="option"
            [id]="id + '-' + index"
            [attr.aria-selected]="option === value()"
            [class.active]="index === active()"
            (mousedown)="$event.preventDefault()"
            (mouseenter)="active.set(index)"
            (click)="pick(option)"
          >
            <app-rich-text [text]="option" />
            @if (option === value()) {
              <app-icon name="check" [size]="16" />
            }
          </li>
        }
      </ul>
    }
  `,
})
export class ChoiceMenuComponent {
  readonly options = input.required<readonly string[]>();
  readonly value = input('');
  readonly label = input('');
  readonly placeholder = input('…');
  readonly disabled = input(false);
  readonly picked = output<string>();

  protected readonly id = `choice-menu-${nextId++}`;
  protected readonly open = signal(false);
  protected readonly active = signal(-1);
  private readonly chosenIndex = computed(() => this.options().indexOf(this.value()));

  protected toggle(): void {
    if (this.open()) {
      this.open.set(false);
    } else {
      this.show();
    }
  }

  protected pick(option: string): void {
    this.open.set(false);
    if (option !== this.value()) {
      this.picked.emit(option);
    }
  }

  protected key(event: KeyboardEvent): void {
    const last = this.options().length - 1;
    if (!this.open()) {
      if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(event.key)) {
        event.preventDefault();
        this.show();
      }
      return;
    }
    const moves: Record<string, number> = {
      ArrowDown: Math.min(this.active() + 1, last),
      ArrowUp: Math.max(this.active() - 1, 0),
      Home: 0,
      End: last,
    };
    if (event.key in moves) {
      event.preventDefault();
      this.active.set(moves[event.key]);
    } else if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      const option = this.options()[this.active()];
      if (option !== undefined) {
        this.pick(option);
      }
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.open.set(false);
    } else if (event.key === 'Tab') {
      this.open.set(false);
    }
  }

  /** Focus leaving the list, by a click elsewhere or by Tab, closes it. */
  protected leave(event: FocusEvent): void {
    const host = event.currentTarget as HTMLElement;
    if (!host.contains(event.relatedTarget as Node | null)) {
      this.open.set(false);
    }
  }

  private show(): void {
    this.active.set(Math.max(this.chosenIndex(), 0));
    this.open.set(true);
  }
}

import { Injectable, effect, signal } from '@angular/core';
import { storedValue, storeValue } from './local-preferences';

export type ThemeChoice = 'auto' | 'light' | 'dark';

/** Light or dark interface: follows the system unless the learner chose. */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly choice = signal<ThemeChoice>(storedValue('theme', ['auto', 'light', 'dark'], 'auto'));

  constructor() {
    effect(() => {
      const choice = this.choice();
      storeValue('theme', choice);
      if (choice === 'auto') {
        document.documentElement.removeAttribute('data-theme');
      } else {
        document.documentElement.setAttribute('data-theme', choice);
      }
    });
  }

  next(): void {
    const order: ThemeChoice[] = ['auto', 'light', 'dark'];
    this.choice.set(order[(order.indexOf(this.choice()) + 1) % order.length]);
  }
}

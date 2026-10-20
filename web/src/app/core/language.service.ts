import { Injectable, inject, signal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { storedValue, storeValue } from './local-preferences';

export type InterfaceLanguage = 'en' | 'fr';
export const LANGUAGES: readonly InterfaceLanguage[] = ['en', 'fr'];

/**
 * The language of the interface: the learner's choice in this browser, else the one of
 * config.yaml, else the browser's. Course content keeps its own language.
 */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly transloco = inject(TranslocoService);
  readonly current = signal<InterfaceLanguage>(initialLanguage());

  constructor() {
    this.apply(this.current());
  }

  /** The language of config.yaml applies unless the learner chose one here. */
  useConfigured(language: string | null): void {
    const chosen = storedValue<InterfaceLanguage | 'none'>('language', [...LANGUAGES, 'none'], 'none');
    const configured = LANGUAGES.find((candidate) => candidate === language);
    if (chosen === 'none' && configured) {
      this.apply(configured);
    }
  }

  toggle(): void {
    const next = this.current() === 'fr' ? 'en' : 'fr';
    storeValue('language', next);
    this.apply(next);
  }

  private apply(language: InterfaceLanguage): void {
    this.current.set(language);
    this.transloco.setActiveLang(language);
    document.documentElement.lang = language;
  }
}

function initialLanguage(): InterfaceLanguage {
  const browser: InterfaceLanguage = navigator.language.toLowerCase().startsWith('fr') ? 'fr' : 'en';
  return storedValue('language', LANGUAGES, browser);
}

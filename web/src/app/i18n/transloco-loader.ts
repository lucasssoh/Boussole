import { Injectable } from '@angular/core';
import { Translation, TranslocoLoader } from '@jsverse/transloco';
import { Observable, of } from 'rxjs';
import { EN } from './en';
import { FR } from './fr';

/** The translations are compiled into the interface: no request, no flash of untranslated text. */
@Injectable({ providedIn: 'root' })
export class BundledTranslationLoader implements TranslocoLoader {
  getTranslation(language: string): Observable<Translation> {
    return of(language === 'fr' ? FR : EN);
  }
}

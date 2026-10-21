import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { LanguageService } from '../core/language.service';

/**
 * The languages a track exists in, the one followed standing out. Shown only when there is a
 * choice; progress is the same in every language. {@code compact} shows codes (FR, EN), for the
 * bar of a session.
 */
@Component({
  selector: 'app-track-languages',
  imports: [TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (languages().length > 1) {
      <span class="track-languages" [class.compact]="compact()" role="group" [attr.aria-label]="'track.languages' | transloco">
        @if (!compact()) {
          <span class="track-languages-label">{{ 'track.followIn' | transloco }}</span>
        }
        @for (language of languages(); track language) {
          <button type="button" class="chip" [class.active]="language === current()" [attr.aria-pressed]="language === current()"
                  [attr.lang]="language" [title]="name(language)" (click)="language !== current() && choose.emit(language)">
            {{ compact() ? language.toUpperCase() : name(language) }}
          </button>
        }
      </span>
    }
  `,
})
export class TrackLanguagesComponent {
  readonly languages = input.required<string[]>();
  readonly current = input.required<string>();
  readonly compact = input(false);
  readonly choose = output<string>();

  private readonly interfaceLanguage = inject(LanguageService);
  private readonly names = computed(() => new Intl.DisplayNames([this.interfaceLanguage.current()], { type: 'language' }));

  /** The language by its own name, as written in it: français, English, español… */
  protected name(language: string): string {
    try {
      const own = new Intl.DisplayNames([language], { type: 'language' }).of(language) ?? language;
      return own.charAt(0).toLocaleUpperCase(language) + own.slice(1);
    } catch {
      return this.names().of(language) ?? language;
    }
  }
}

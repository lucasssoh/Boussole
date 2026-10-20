import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { IconComponent } from '../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { LANGUAGES, LanguageService } from './language.service';
import { ThemeChoice, ThemeService } from './theme.service';

/**
 * The gear of the header: a small panel pulled down under it, with the theme and the language.
 * Opening, the panel comes first and its contents pop in one after the other; closing, they go
 * in the reverse order (--o) and the panel last.
 */
@Component({
  selector: 'app-settings-drawer',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'open.set(false)' },
  template: `
    <button
      type="button"
      class="quiet gear"
      [attr.aria-label]="'settings.title' | transloco"
      [attr.aria-expanded]="open()"
      aria-controls="settings-drawer"
      (click)="open.set(!open())"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <circle cx="12" cy="12" r="3"></circle>
        <path d="M19.4 15a1.7 1.7 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.3 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.3l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.3-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.3-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.3H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.3l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.3 1.8V9a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z"></path>
      </svg>
    </button>
    @if (open()) {
      <button type="button" class="drawer-backdrop" tabindex="-1" [attr.aria-label]="'panel.close' | transloco" (click)="open.set(false)"></button>
      <aside id="settings-drawer" class="settings-drawer" animate.leave="leaving" role="dialog" [attr.aria-label]="'settings.title' | transloco">
        <div class="drawer-head pop" style="--i: 1; --o: 8">
          <h2>{{ 'settings.title' | transloco }}</h2>
          <button type="button" class="quiet" [attr.aria-label]="'panel.close' | transloco" (click)="open.set(false)"><app-icon name="close" /></button>
        </div>
        <fieldset>
          <legend class="pop" style="--i: 2; --o: 7">{{ 'settings.theme' | transloco }}</legend>
          <div class="choice-row">
            @for (choice of themes; track choice) {
              <button type="button" class="pop" [style.--i]="3 + $index" [style.--o]="6 - $index" [class.primary]="theme.choice() === choice" [attr.aria-pressed]="theme.choice() === choice" (click)="theme.set(choice)">
                {{ 'settings.themes.' + choice | transloco }}
              </button>
            }
          </div>
        </fieldset>
        <fieldset>
          <legend class="pop" style="--i: 6; --o: 3">{{ 'settings.language' | transloco }}</legend>
          <div class="choice-row">
            @for (code of languages; track code) {
              <button type="button" class="pop" [style.--i]="7 + $index" [style.--o]="2 - $index" [class.primary]="language.current() === code" [attr.aria-pressed]="language.current() === code" (click)="language.set(code)">
                {{ 'settings.languages.' + code | transloco }}
              </button>
            }
          </div>
        </fieldset>
      </aside>
    }
  `,
})
export class SettingsDrawerComponent {
  protected readonly theme = inject(ThemeService);
  protected readonly language = inject(LanguageService);
  protected readonly open = signal(false);
  protected readonly themes: ThemeChoice[] = ['auto', 'light', 'dark'];
  protected readonly languages = LANGUAGES;
}

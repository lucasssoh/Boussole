import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { BoussoleApi } from './core/boussole-api';
import { LanguageService } from './core/language.service';
import { ThemeService } from './core/theme.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TranslocoPipe],
  template: `
    <header class="top-bar">
      <a class="brand" routerLink="/"><span aria-hidden="true">🧭</span> Boussole</a>
      <nav class="top-actions">
        <button type="button" class="quiet" (click)="language.toggle()">{{ 'app.otherLanguage' | transloco }}</button>
        <button type="button" class="quiet" (click)="theme.next()">{{ 'app.theme.' + theme.choice() | transloco }}</button>
      </nav>
    </header>
    <router-outlet />
  `,
  styles: `
    .top-bar {
      position: sticky;
      top: 0;
      z-index: 20;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.5rem 1rem;
      background: var(--surface);
      border-bottom: 1px solid var(--line);
    }
    .brand {
      font-weight: 700;
      color: var(--text);
      text-decoration: none;
    }
    .top-actions {
      display: flex;
      gap: 0.25rem;
    }
  `,
})
export class App {
  protected readonly theme = inject(ThemeService);
  protected readonly language = inject(LanguageService);

  constructor() {
    inject(BoussoleApi)
      .server()
      .then((server) => this.language.useConfigured(server.language))
      .catch(() => undefined);
  }
}

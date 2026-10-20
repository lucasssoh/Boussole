import { Injectable, inject, signal } from '@angular/core';
import { BoussoleApi } from './boussole-api';
import { LanguageService } from './language.service';

/** What the server says of the configuration, asked once when the interface starts. */
@Injectable({ providedIn: 'root' })
export class ServerSettingsService {
  private readonly api = inject(BoussoleApi);
  private readonly language = inject(LanguageService);

  readonly freeNavigation = signal(false);

  load(): void {
    this.api
      .server()
      .then((server) => {
        this.language.useConfigured(server.language);
        this.freeNavigation.set(server.freeNavigation);
      })
      .catch(() => undefined);
  }
}

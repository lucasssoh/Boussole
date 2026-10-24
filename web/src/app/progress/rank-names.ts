import { Injectable, inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { LanguageService } from '../core/language.service';
import { formatPoints, roman } from '../shared/ranks';

/** Names of ranks and fields, and points, in the language of the interface. */
@Injectable({ providedIn: 'root' })
export class RankNames {
  private readonly transloco = inject(TranslocoService);
  private readonly language = inject(LanguageService);

  rank(tier: string, division: number): string {
    const name = this.transloco.translate('rank.' + tier, {}, this.language.current());
    return division > 0 ? `${name} ${roman(division)}` : name;
  }

  field(domain: string): string {
    return this.transloco.translate('domain.' + domain, {}, this.language.current());
  }

  points(points: number): string {
    return formatPoints(points, this.language.current());
  }
}

import { Injectable, computed, inject, signal } from '@angular/core';
import { StudyStats } from './api';
import { BoussoleApi } from './boussole-api';
import { storeValue, storedText } from './local-preferences';
import { CULTURE_TIERS, rankOrder } from '../shared/ranks';
import { Reached } from '../shared/promotion.component';

/** The rank last shown to the learner on this browser, as `tier:division`. */
const SEEN = 'rank.seen';

/**
 * The learner's points, ranks and streak, shown in the header and refreshed after each answer;
 * and the division newly reached since the learner last saw their rank, until they close it. During
 * a session, it waits: nothing comes between the learner and the question, and the moment is
 * shown once the session is over, from the rank the learner started from to the one reached.
 */
@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly api = inject(BoussoleApi);

  readonly stats = signal<StudyStats | null>(null);
  readonly promotion = signal<{ from: Reached; to: Reached } | null>(null);
  /** Whether a session is being studied. */
  readonly studying = signal(false);
  readonly shownPromotion = computed(() => (this.studying() ? null : this.promotion()));

  refresh(): void {
    this.api.stats().then((stats) => {
      this.stats.set(stats);
      this.notice({ tier: stats.culture.tier, division: stats.culture.division });
    }, () => undefined);
  }

  closePromotion(): void {
    this.promotion.set(null);
  }

  /** A higher division than the one last seen is shown once; the first visit only remembers it. */
  private notice(reached: Reached): void {
    const seen = parse(storedText(SEEN));
    storeValue(SEEN, `${reached.tier}:${reached.division}`);
    if (seen && rankOrder(reached) > rankOrder(seen)) {
      this.promotion.update((waiting) => ({ from: waiting?.from ?? seen, to: reached }));
    }
  }
}

function parse(text: string): Reached | null {
  const [tier, division] = text.split(':');
  return tier && CULTURE_TIERS.includes(tier) ? { tier, division: Number(division) || 0 } : null;
}

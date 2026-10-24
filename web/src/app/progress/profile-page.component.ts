import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { AchievementProgress, FieldStanding, LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { emblemAmbiance, emblemInk, emblemLook } from '../shared/emblem';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { SpecialtyEmblemComponent } from '../shared/specialty-emblem.component';
import { CRITICAL_THINKING, divisionProgress, isStrong, roman } from '../shared/ranks';
import { RankNames } from './rank-names';
import { RankRouteComponent } from './rank-route.component';

/**
 * The learner's page: their card (emblem, culture rank, level, points, records), the route of the
 * divisions with theirs in the middle, the next division, the specialties Sage asks for, each specialty, and the
 * achievements, kept for good.
 */
@Component({
  selector: 'app-profile-page',
  imports: [RankEmblemComponent, RankRouteComponent, RouterLink, SpecialtyEmblemComponent, TranslocoPipe],
  host: { class: 'profile' },
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (stats.stats(); as current) {
      @let rank = current.culture;
      <section class="profile-hero" [style.--sky]="ambiance()" [style.--accent]="accent()" [style.color]="ink()">
        <div class="profile-card wide">
          <div class="profile-emblem"><app-rank-emblem [tier]="rank.tier" [division]="rank.division" [size]="184" [lively]="true" /></div>
          <div class="profile-who">
            <span class="profile-ladder">{{ 'profile.culture' | transloco }}</span>
            <h1>{{ names.rank(rank.tier, rank.division) }}</h1>
            <span class="profile-level">
              <span class="level-chip">{{ rank.level }}</span>
              {{ 'profile.points' | transloco: { points: names.points(current.points) } }}
            </span>
            @if (next(); as cap) {
              <span class="profile-next" [title]="'profile.next' | transloco">
                <span class="xp-track"><span class="xp-fill" [style.width.%]="100 * progress()" [style.background]="accent()"></span></span>
                {{ 'profile.toGo' | transloco: { points: names.points(toGo(cap.from, current.points)) } }} · {{ names.rank(cap.tier, cap.division) }}
              </span>
            }
            <a class="profile-ranks" routerLink="/ranks">{{ 'profile.ranks' | transloco }}</a>
          </div>
          <dl class="profile-records">
            <div>
              <dt>{{ 'profile.bestStreak' | transloco }}</dt>
              <dd>{{ 'profile.days' | transloco: { days: current.records.bestStreak } }}</dd>
            </div>
            <div>
              <dt>{{ 'profile.flawless' | transloco }}</dt>
              <dd>{{ current.records.flawlessSessions }}</dd>
            </div>
            <div>
              <dt>{{ 'profile.firstTry' | transloco }}</dt>
              <dd>{{ firstTry() }}</dd>
            </div>
          </dl>
        </div>
        @if (cultureSteps().length > 0) {
          <app-rank-route [steps]="cultureSteps()" [here]="hereIndex()" />
        }
      </section>

      <section class="profile-zone">
        <div class="wide">
          <div class="zone-head">
            <h2>{{ 'profile.specialties' | transloco }}</h2>
            <span class="need" role="img" [attr.aria-label]="'profile.specialistsCount' | transloco: { count: strongSpecialties(), needed: 3 }" [title]="'profile.atSpecialist' | transloco">
              {{ (towardGrandSage() ? 'profile.forGrandSage' : 'profile.forSage') | transloco }}
              @for (met of specialtyMarks(); track $index) {
                <span class="pip" [class.met]="met"></span>
              }
              @if (towardGrandSage()) {
                <span class="need-plus" aria-hidden="true">+</span>
                <span class="pip critical" [class.met]="criticalThinking()" [title]="'profile.criticalThinking' | transloco"></span>
              }
            </span>
          </div>
          @if (current.fields.length === 0) {
            <p class="note">{{ 'profile.noSpecialty' | transloco }}</p>
          }
          <div class="specialties">
            @for (field of current.fields; track field.domain) {
              <section class="specialty" [title]="'profile.points' | transloco: { points: names.points(field.points) }">
                <app-specialty-emblem [tier]="field.specialty.tier" [size]="56" [progress]="specialtyProgress(field)" />
                <div>
                  <strong>{{ names.field(field.domain) }}</strong>
                  <span class="specialty-rank">{{ names.rank(field.specialty.tier, 0) }}</span>
                </div>
              </section>
            }
          </div>
        </div>
      </section>

      <section class="profile-zone trophies">
        <div class="wide">
          <div class="zone-head">
            <h2>{{ 'profile.achievements' | transloco }}</h2>
            <span class="count">{{ 'profile.achievementsCount' | transloco: { reached: levelsReached(), total: levelsTotal() } }}</span>
          </div>
          @if (reached().length === 0) {
            <p class="note">{{ 'profile.noAchievement' | transloco }}</p>
          }
          <div class="medals">
            @for (achievement of reached(); track achievement.id) {
              <section class="medal" [title]="('achievement.' + achievement.id + '.description' | transloco) + ' · ' + progressText(achievement)">
                <span class="medal-disc" aria-hidden="true">{{ roman(achievement.level) }}</span>
                <strong>{{ 'achievement.' + achievement.id + '.name' | transloco }}</strong>
              </section>
            }
          </div>
        </div>
      </section>
    }
  `,
})
export class ProfilePageComponent {
  protected readonly stats = inject(StatsService);
  protected readonly names = inject(RankNames);
  protected readonly roman = roman;

  /** The colour that reads on the banner: the tier's, or the gold of the Grand Sage. */
  protected readonly accent = computed(() => emblemLook(this.stats.stats()?.culture.tier ?? 'curious').auraColor);
  /** The sky at the height of the learner's tier, behind their card, and the ink that reads on it. */
  protected readonly ambiance = computed(() => emblemAmbiance(this.stats.stats()?.culture.tier ?? 'curious'));
  protected readonly ink = computed(() => emblemInk(this.stats.stats()?.culture.tier ?? 'curious'));
  protected readonly firstTry = computed(() => {
    const records = this.stats.stats()?.records;
    return records && records.firstTryAnswered > 0 ? `${Math.round((100 * records.firstTryRight) / records.firstTryAnswered)} %` : '—';
  });
  /** The division after the learner's, in the route. */
  protected readonly next = computed(() => {
    const current = this.stats.stats();
    if (!current) {
      return null;
    }
    const here = current.route.findIndex((step) => this.isHere(step));
    return here >= 0 ? (current.route[here + 1] ?? null) : null;
  });
  protected readonly progress = computed(() => {
    const current = this.stats.stats();
    return current ? divisionProgress(current.culture, current.points) : 0;
  });
  /** Past Sage, the specialties count besides critical thinking, which Grand Sage asks for too. */
  protected readonly towardGrandSage = computed(() => {
    const tier = this.stats.stats()?.culture.tier;
    return tier === 'sage' || tier === 'grand-sage';
  });
  protected readonly strongSpecialties = computed(() =>
    (this.stats.stats()?.fields ?? [])
      .filter((field) => isStrong(field.specialty.tier) && !(this.towardGrandSage() && field.domain === CRITICAL_THINKING)).length,
  );
  protected readonly criticalThinking = computed(() =>
    (this.stats.stats()?.fields ?? []).some((field) => field.domain === CRITICAL_THINKING && isStrong(field.specialty.tier)),
  );
  protected readonly specialtyMarks = computed(() => Array.from({ length: 3 }, (_, index) => index < this.strongSpecialties()));
  /** Every division of the culture, and where the learner's is among them. */
  protected readonly cultureSteps = signal<LadderStep[]>([]);
  protected readonly hereIndex = computed(() => Math.max(0, this.cultureSteps().findIndex((step) => this.isHere(step))));
  /** Where each specialty tier starts, to fill the ring up to the next one. */
  private readonly specialtyTiers = signal<LadderStep[]>([]);
  private readonly allAchievements = signal<AchievementProgress[]>([]);
  /** The achievements reached, the highest first. */
  protected readonly reached = computed(() =>
    this.allAchievements().filter((achievement) => achievement.level > 0).sort((a, b) => b.level - a.level || this.toNext(b) - this.toNext(a)),
  );
  protected readonly levelsReached = computed(() => this.allAchievements().reduce((sum, achievement) => sum + achievement.level, 0));
  protected readonly levelsTotal = computed(() => this.allAchievements().reduce((sum, achievement) => sum + achievement.thresholds.length, 0));

  constructor() {
    this.stats.refresh();
    const api = inject(BoussoleApi);
    api.achievements().then((achievements) => this.allAchievements.set(achievements), () => undefined);
    api.ranks().then((ladders) => {
      this.cultureSteps.set(ladders.culture);
      this.specialtyTiers.set(ladders.specialty.filter((step, index, all) => all[index - 1]?.tier !== step.tier));
    }, () => undefined);
  }

  /** The value counted, out of what the next level asks; the value alone once every level is reached. */
  protected progressText(achievement: AchievementProgress): string {
    const unit = achievement.id === 'trial-by-fire' ? ' %' : '';
    const next = achievement.thresholds[achievement.level];
    return next === undefined ? `${achievement.value}${unit}` : `${achievement.value} / ${next}${unit}`;
  }

  private toNext(achievement: AchievementProgress): number {
    const next = achievement.thresholds[achievement.level];
    return next === undefined ? 1 : achievement.value / next;
  }

  /** How far a specialty is from the start of its tier to the next; full at the top. */
  protected specialtyProgress(field: FieldStanding): number | null {
    const tiers = this.specialtyTiers();
    const index = tiers.findIndex((step) => step.tier === field.specialty.tier);
    if (index < 0) {
      return null;
    }
    const next = tiers[index + 1];
    return next ? (field.points - tiers[index].from) / (next.from - tiers[index].from) : 1;
  }

  protected toGo(from: number, points: number): number {
    return Math.max(0, Math.ceil(from - points));
  }

  private isHere(step: LadderStep): boolean {
    const rank = this.stats.stats()?.culture;
    return !!rank && step.tier === rank.tier && step.division === Math.max(rank.division, 0);
  }
}

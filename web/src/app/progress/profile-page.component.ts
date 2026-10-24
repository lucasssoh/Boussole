import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { AchievementProgress, LadderStep } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { StatsService } from '../core/stats.service';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { CRITICAL_THINKING, divisionProgress, isStrong, roman, tierColors } from '../shared/ranks';
import { RankNames } from './rank-names';

/**
 * The learner's page: their card (emblem, culture rank, level, points, records), the route of the
 * divisions around theirs, the next division, the specialties Sage asks for, each specialty, and the
 * achievements, kept for good.
 */
@Component({
  selector: 'app-profile-page',
  imports: [RankEmblemComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page wide profile">
      @if (stats.stats(); as current) {
        @let rank = current.culture;
        <section class="profile-banner" [style.--tier]="colors()[0]" [style.--tier-deep]="colors()[1]">
          <div class="profile-card">
            <div class="profile-emblem"><app-rank-emblem [tier]="rank.tier" [size]="112" /></div>
            <div class="profile-who">
              <span class="profile-ladder">{{ 'profile.culture' | transloco }}</span>
              <h1>{{ names.rank(rank.tier, rank.division) }}</h1>
              <span class="profile-level">
                <span class="level-chip">{{ rank.level }}</span>
                {{ 'profile.points' | transloco: { points: names.points(current.points) } }}
              </span>
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
          <ol class="route" [attr.aria-label]="'profile.route' | transloco">
            @for (step of current.route; track step.firstLevel) {
              <li [class]="'route-step ' + stepState(step)" [style.--step]="stepColor(step)" [title]="names.rank(step.tier, step.division)">
                <span>{{ step.division > 0 ? roman(step.division) : '∞' }}</span>
              </li>
            }
          </ol>
        </section>

        <div class="profile-cards">
          @if (next(); as cap) {
            <section class="profile-panel">
              <span class="panel-label">{{ 'profile.next' | transloco }}</span>
              <strong class="panel-value">{{ names.rank(cap.tier, cap.division) }}</strong>
              <span class="panel-note">{{ 'profile.toGo' | transloco: { points: names.points(toGo(cap.from, current.points)) } }}</span>
              <div class="xp-track"><span class="xp-fill" [style.width.%]="100 * progress()" [style.background]="colors()[0]"></span></div>
              <a class="button" routerLink="/ranks">{{ 'profile.ranks' | transloco }}</a>
            </section>
          }
          <section class="profile-panel">
            <span class="panel-label">{{ (towardGrandSage() ? 'profile.forGrandSage' : 'profile.forSage') | transloco }}</span>
            <strong class="panel-value">{{ 'profile.specialistsCount' | transloco: { count: strongSpecialties(), needed: 3 } }}</strong>
            <div class="field-marks">
              @for (mark of specialtyMarks(); track $index) {
                <span class="field-mark" [class.filled]="mark"></span>
              }
            </div>
            <span class="panel-note">{{ 'profile.atSpecialist' | transloco }}</span>
            @if (towardGrandSage()) {
              <span class="panel-note" [class.met]="criticalThinking()">{{ 'profile.criticalThinking' | transloco }}</span>
            }
          </section>
        </div>

        <h2>{{ 'profile.specialties' | transloco }}</h2>
        @if (current.fields.length === 0) {
          <p class="note">{{ 'profile.noSpecialty' | transloco }}</p>
        }
        <div class="specialties">
          @for (field of current.fields; track field.domain) {
            <section class="specialty" [style.--tier]="fieldColor(field.specialty.tier)">
              <app-rank-emblem [tier]="field.specialty.tier" [size]="34" />
              <div>
                <strong>{{ names.field(field.domain) }}</strong>
                <span class="specialty-rank">{{ names.rank(field.specialty.tier, field.specialty.division) }}</span>
                <span class="panel-note">{{ 'profile.points' | transloco: { points: names.points(field.points) } }}</span>
              </div>
            </section>
          }
        </div>

        <h2>{{ 'profile.achievements' | transloco }} <span class="count">{{ 'profile.achievementsCount' | transloco: { reached: levelsReached(), total: levelsTotal() } }}</span></h2>
        <div class="achievements">
          @for (achievement of achievements(); track achievement.id) {
            <section class="achievement" [class.reached]="achievement.level > 0">
              <span class="achievement-frame" aria-hidden="true">{{ achievement.level > 0 ? roman(achievement.level) : '' }}</span>
              <div>
                <strong>{{ 'achievement.' + achievement.id + '.name' | transloco }}</strong>
                <span class="achievement-what">{{ 'achievement.' + achievement.id + '.description' | transloco }}</span>
                <span class="achievement-progress">{{ progressText(achievement) }}</span>
              </div>
            </section>
          }
        </div>
      }
    </main>
  `,
})
export class ProfilePageComponent {
  protected readonly stats = inject(StatsService);
  protected readonly names = inject(RankNames);
  protected readonly roman = roman;

  protected readonly colors = computed(() => tierColors(this.stats.stats()?.culture.tier ?? 'curious'));
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
  private readonly allAchievements = signal<AchievementProgress[]>([]);
  /** Those reached first, the highest first; then the nearest to their next level. */
  protected readonly achievements = computed(() =>
    [...this.allAchievements()].sort((a, b) => b.level - a.level || this.toNext(b) - this.toNext(a)),
  );
  protected readonly levelsReached = computed(() => this.allAchievements().reduce((sum, achievement) => sum + achievement.level, 0));
  protected readonly levelsTotal = computed(() => this.allAchievements().reduce((sum, achievement) => sum + achievement.thresholds.length, 0));

  constructor() {
    this.stats.refresh();
    inject(BoussoleApi).achievements().then((achievements) => this.allAchievements.set(achievements), () => undefined);
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

  protected stepState(step: LadderStep): string {
    if (this.isHere(step)) {
      return 'here';
    }
    const current = this.stats.stats();
    return current && step.from <= current.culture.divisionFrom ? 'past' : 'next';
  }

  protected stepColor(step: LadderStep): string {
    return tierColors(step.tier)[0];
  }

  protected fieldColor(tier: string): string {
    return tierColors(tier)[0];
  }

  protected toGo(from: number, points: number): number {
    return Math.max(0, Math.ceil(from - points));
  }

  private isHere(step: LadderStep): boolean {
    const rank = this.stats.stats()?.culture;
    return !!rank && step.tier === rank.tier && step.division === Math.max(rank.division, 0);
  }
}

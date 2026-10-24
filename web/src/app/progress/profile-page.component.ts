import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LadderStep } from '../core/api';
import { StatsService } from '../core/stats.service';
import { RankEmblemComponent } from '../shared/rank-emblem.component';
import { divisionProgress, roman, tierColors } from '../shared/ranks';
import { RankNames } from './rank-names';

/** The share of the points a field must hold to count for Sage and Grand Sage. */
const FIELD_SHARE = 0.1;

/**
 * The learner's page: their card (emblem, culture rank, level, points, records), the route of the
 * divisions around theirs, the next division, the fields Sage asks for, and each specialty.
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
            <span class="panel-label">{{ (rank.tier === 'sage' || rank.tier === 'grand-sage' ? 'profile.forGrandSage' : 'profile.forSage') | transloco }}</span>
            <strong class="panel-value">{{ 'profile.fields' | transloco: { count: wideFields(), needed: fieldsNeeded() } }}</strong>
            <div class="field-marks">
              @for (mark of fieldMarks(); track $index) {
                <span class="field-mark" [class.filled]="mark"></span>
              }
            </div>
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
  protected readonly wideFields = computed(() => {
    const current = this.stats.stats();
    return current ? current.fields.filter((field) => current.points > 0 && field.points >= FIELD_SHARE * current.points).length : 0;
  });
  protected readonly fieldsNeeded = computed(() => {
    const tier = this.stats.stats()?.culture.tier;
    return tier === 'sage' || tier === 'grand-sage' ? 5 : 3;
  });
  protected readonly fieldMarks = computed(() =>
    Array.from({ length: this.fieldsNeeded() }, (_, index) => index < this.wideFields()),
  );

  constructor() {
    this.stats.refresh();
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

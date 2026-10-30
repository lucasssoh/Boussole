import { ChangeDetectionStrategy, Component, booleanAttribute, computed, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { MAX_STARS, StarTone } from './stars';

let nextId = 0;

/** The outline of a star, on a 24 × 24 grid; its edge is the same outline, lower. */
const STAR = 'M12 2.6l2.85 5.9 6.5.92-4.7 4.55 1.13 6.43L12 17.35l-5.78 3.05 1.13-6.43-4.7-4.55 6.5-.92z';

/**
 * The stars of an answer, in relief like the buttons: a face lit from above, a darker edge below,
 * a glint. With {@link play}, they arrive one by one: earned stars pop, empty ones settle, blue ones
 * rise, outlined ones draw themselves, and three gold stars end on a burst; {@link delay} holds
 * them back, hidden, so that a list fills in turn. Nothing moves when motion is reduced.
 */
@Component({
  selector: 'app-stars',
  imports: [TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class]': "'stars ' + tone() + (play() ? ' play' : '') + (count() === slots().length && slots().length === 3 ? ' full' : '')",
    '[style.--delay]': "delay() + 's'",
  },
  template: `
    @if (!decorative()) {
      <span class="visually-hidden">{{ 'stars.' + tone() | transloco: { count: count() } }}</span>
    }
    @for (slot of slots(); track slot) {
      <svg class="star" [class.on]="slot < count()" [class.off]="slot >= count()" [style.--i]="slot" viewBox="0 0 24 25" aria-hidden="true">
        @if (slot === 0) {
          <defs>
            <linearGradient [attr.id]="id + '-on'" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0" class="star-stop-on-top"></stop>
              <stop offset="1" class="star-stop-on-bottom"></stop>
            </linearGradient>
            <linearGradient [attr.id]="id + '-off'" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0" class="star-stop-off-top"></stop>
              <stop offset="1" class="star-stop-off-bottom"></stop>
            </linearGradient>
          </defs>
        }
        @if (tone() === 'outline') {
          <path class="star-trace star-trace-edge" pathLength="1" [attr.d]="star" transform="translate(0 1.8)"></path>
          <path class="star-trace" pathLength="1" [attr.d]="star"></path>
        } @else {
          <path class="star-edge" [attr.d]="star" transform="translate(0 1.9)"></path>
          <path class="star-face" [attr.d]="star" [style.fill]="'url(#' + id + (slot < count() ? '-on' : '-off') + ')'"></path>
          <path class="star-glint" d="M7.6 9.9l2.7-.4 1.6-3.3"></path>
          @if (tone() === 'gold' && slot === 2) {
            <path class="star-rays" d="M12 -1.5v-3M23 5.5l2.6-1.6M1 5.5l-2.6-1.6M20.5 21.5l1.8 2.4M3.5 21.5l-1.8 2.4"></path>
          }
        }
      </svg>
    }
  `,
})
export class StarsComponent {
  /** How many stars are earned, of {@link max}. */
  readonly count = input.required<number>();
  readonly tone = input<StarTone>('gold');
  readonly max = input(MAX_STARS);
  readonly play = input(false);
  /** Seconds before the first star arrives. */
  readonly delay = input(0);
  /** The text beside says what the stars mean: they are not read out. */
  readonly decorative = input(false, { transform: booleanAttribute });

  protected readonly id = `stars-${nextId++}`;
  protected readonly star = STAR;
  protected readonly slots = computed(() => Array.from({ length: this.max() }, (_, index) => index));
}

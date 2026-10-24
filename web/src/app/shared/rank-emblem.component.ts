import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { tierColors } from './ranks';

/** The emblem of a tier: a shield in its colours, with the needle of the compass. Decorative. */
@Component({
  selector: 'app-rank-emblem',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.width]="size()" [attr.height]="size() * 1.125" viewBox="0 0 64 72" aria-hidden="true">
      <path d="M32 3 L59 13 V36 C59 51 48 63 32 69 C16 63 5 51 5 36 V13 Z" [attr.fill]="colors()[0]" [attr.stroke]="colors()[1]" stroke-width="3" stroke-linejoin="round"></path>
      <path d="M32 17 L38 36 L32 55 L26 36 Z" [attr.fill]="colors()[1]"></path>
      <path d="M32 17 L38 36 L26 36 Z" fill="#ffffff"></path>
    </svg>
  `,
  host: { class: 'rank-emblem' },
})
export class RankEmblemComponent {
  readonly tier = input.required<string>();
  readonly size = input(28);

  protected readonly colors = computed(() => tierColors(this.tier()));
}

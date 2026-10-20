import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { IconComponent } from '../../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { QuestionProgress } from '../../core/api';

/** The line above a question: whether it is asked again, its time, and whether it is solved. */
@Component({
  selector: 'app-question-header',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="question-id">{{ id() }}</span>
    @if (reused()) {
      <span class="tag">{{ 'question.reused' | transloco }}</span>
    }
    <span class="tag">{{ 'question.minutes' | transloco: { minutes: minutes() } }}</span>
    @if (progress().solved) {
      <span class="tag solved"><app-icon name="check" [size]="14" /> {{ 'question.solved' | transloco }}</span>
    }
  `,
  host: { class: 'question-header' },
})
export class QuestionHeaderComponent {
  readonly id = input.required<string>();
  readonly minutes = input.required<number>();
  readonly reused = input(false);
  readonly progress = input.required<QuestionProgress>();
}

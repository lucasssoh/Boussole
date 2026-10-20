import { ChangeDetectionStrategy, Component, computed, forwardRef, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * The correction under a question, once it is answered: the verdict and the experience earned,
 * what was expected (projected by the question, [expected]) and why it is right (the solution of
 * the file). A free exercise shows it before the learner grades their answer.
 */
@Component({
  selector: 'app-question-correction',
  imports: [TranslocoPipe, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'correction', role: 'status', '[class]': "'correction ' + (verdict() ?? 'neutral')" },
  template: `
    <div class="correction-head">
      <span class="correction-title">
        {{ (verdict() ? 'verdict.' + verdict() : 'question.solutionTitle') | transloco }}
      </span>
      @if (state().result(); as result) {
        @if (result.xpGained > 0) {
          <span class="xp-pill">+{{ result.xpGained }} XP</span>
        }
      }
    </div>
    <ng-content select="[expected]" />
    @if (state().solution(); as solution) {
      @if (solution.blocks.length > 0) {
        <div class="correction-why">
          <span class="correction-label">{{ 'question.why' | transloco }}</span>
          <app-block-list [blocks]="solution.blocks" />
        </div>
      }
    }
  `,
})
export class QuestionCorrectionComponent {
  readonly state = input.required<QuestionState>();

  protected readonly verdict = computed(() => this.state().result()?.verdict ?? null);
}

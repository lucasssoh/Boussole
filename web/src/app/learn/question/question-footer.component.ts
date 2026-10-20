import { ChangeDetectionStrategy, Component, forwardRef, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * The part every question shares: the buttons (check, next hint, solution), the verdict, the
 * hints revealed and the solution. The question shows what was expected in the slot [expected].
 */
@Component({
  selector: 'app-question-footer',
  imports: [TranslocoPipe, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let question = state();
    <div class="question-actions">
      @if (checkable()) {
        <button type="button" class="primary" [disabled]="question.busy() || !ready()" (click)="check.emit()">
          {{ 'question.check' | transloco }}
        </button>
      }
      @if (question.hintsLeft()) {
        <button type="button" [disabled]="question.busy()" (click)="question.nextHint()">
          {{ 'question.hint' | transloco }} ({{ question.hints().length + 1 }}/{{ question.hintTotal }})
        </button>
      }
      @if (!question.solution()) {
        <button
          type="button"
          [disabled]="question.busy() || !question.solutionAllowed()"
          [title]="question.solutionAllowed() ? '' : ('question.solutionLocked' | transloco)"
          (click)="question.showSolution()"
        >
          {{ 'question.solution' | transloco }}
        </button>
      }
      @if (question.result(); as result) {
        <span class="verdict" [class]="result.verdict" role="status">{{ 'verdict.' + result.verdict | transloco }}</span>
      }
    </div>
    @if (question.error(); as error) {
      <p class="error" role="alert">{{ 'question.failed' | transloco: { message: error } }}</p>
    }
    @for (hint of question.hints(); track $index) {
      <aside class="frame hint">
        <h4 class="frame-title">{{ 'question.hintCount' | transloco: { number: $index + 1, total: question.hintTotal } }}</h4>
        <app-block-list [blocks]="hint" />
      </aside>
    }
    @if (question.solution(); as solution) {
      <section class="frame solution">
        <h4 class="frame-title">{{ 'question.solutionTitle' | transloco }}</h4>
        <ng-content select="[expected]" />
        <app-block-list [blocks]="solution.blocks" />
      </section>
    }
  `,
})
export class QuestionFooterComponent {
  readonly state = input.required<QuestionState>();
  /** False for a free exercise: it is graded by the learner, after the solution. */
  readonly checkable = input(true);
  /** Whether the response is complete enough to be checked. */
  readonly ready = input(true);
  readonly check = output<void>();
}

import { ChangeDetectionStrategy, Component, forwardRef, input, output } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * Below the response of every question: the button that sends it (once), the hints to reveal
 * before that, and the verdict after it.
 */
@Component({
  selector: 'app-question-footer',
  imports: [TranslocoPipe, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let question = state();
    @for (hint of question.hints(); track $index) {
      <aside class="frame hint">
        <h4 class="frame-title">{{ 'question.hintCount' | transloco: { number: $index + 1, total: question.hintTotal } }}</h4>
        <app-block-list [blocks]="hint" />
      </aside>
    }
    <div class="question-actions">
      @if (checkable() && !question.answered()) {
        <button type="button" class="primary" [disabled]="question.busy() || !ready()" (click)="check.emit()">
          {{ 'question.check' | transloco }}
        </button>
      }
      @if (question.isFree && !question.solution()) {
        <button type="button" class="primary" [disabled]="question.busy()" (click)="question.showSolution()">
          {{ 'free.compare' | transloco }}
        </button>
      }
      @if (question.hintsLeft()) {
        <button type="button" [disabled]="question.busy()" (click)="question.nextHint()">
          💡 {{ 'question.hint' | transloco }} ({{ question.hints().length + 1 }}/{{ question.hintTotal }})
        </button>
      }
      @if (question.result(); as result) {
        @if (question.correctionShown()) {
          <span class="verdict" [class]="result.verdict" role="status">{{ 'verdict.' + result.verdict | transloco }}</span>
        } @else {
          <span class="verdict saved" role="status">{{ 'question.saved' | transloco }}</span>
        }
      }
    </div>
    @if (question.error(); as error) {
      <p class="error" role="alert">{{ 'question.failed' | transloco: { message: error } }}</p>
    }
  `,
})
export class QuestionFooterComponent {
  readonly state = input.required<QuestionState>();
  /** False for a free exercise: it is graded by the learner, after the solution. */
  readonly checkable = input(true);
  /** Whether the response is complete enough to be sent. */
  readonly ready = input(true);
  readonly check = output<void>();
}

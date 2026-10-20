import { ChangeDetectionStrategy, Component, forwardRef, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * The correction beside a question, once it is answered: what was expected (projected by the
 * question, [expected]) and why it is right (the solution of the file).
 */
@Component({
  selector: 'app-question-correction',
  imports: [TranslocoPipe, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'correction', role: 'note' },
  template: `
    <h4 class="frame-title">{{ 'question.why' | transloco }}</h4>
    <ng-content select="[expected]" />
    @if (state().solution(); as solution) {
      <app-block-list [blocks]="solution.blocks" />
    }
  `,
})
export class QuestionCorrectionComponent {
  readonly state = input.required<QuestionState>();
}

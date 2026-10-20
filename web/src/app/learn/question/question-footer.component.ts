import { ChangeDetectionStrategy, Component, forwardRef, input, output, signal } from '@angular/core';
import { IconComponent } from '../../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * Below the response of every question: the button that sends it (once), the hints to reveal
 * before that, and the verdict after it.
 */
@Component({
  selector: 'app-question-footer',
  imports: [IconComponent, TranslocoPipe, forwardRef(() => BlockListComponent)],
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
        @if (confirming(); as message) {
          <span class="confirm" role="alertdialog" [attr.aria-label]="message">
            <span class="confirm-text">{{ message }}</span>
            <button type="button" class="primary" [disabled]="question.busy()" (click)="send()">{{ 'question.confirmYes' | transloco }}</button>
            <button type="button" (click)="confirming.set(null)">{{ 'question.confirmNo' | transloco }}</button>
          </span>
        } @else {
          <button type="button" class="primary" [disabled]="question.busy() || !ready()" (click)="ask()">
            {{ 'question.check' | transloco }}
          </button>
          @if (incomplete() && !ready()) {
            <span class="note incomplete">{{ 'question.incomplete' | transloco }}</span>
          }
        }
      }
      @if (question.isFree && !question.solution()) {
        <button type="button" class="primary" [disabled]="question.busy()" (click)="question.showSolution()">
          {{ 'free.compare' | transloco }}
        </button>
      }
      @if (question.hintsLeft()) {
        <button type="button" [disabled]="question.busy()" (click)="question.nextHint()">
          <app-icon name="hint" /> {{ 'question.hint' | transloco }} ({{ question.hints().length + 1 }}/{{ question.hintTotal }})
        </button>
      }
      @if (question.result() && !question.correctionShown()) {
        <span class="verdict saved" role="status">{{ 'question.saved' | transloco }}</span>
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
  /** The response has several fields: say why it cannot be sent yet. */
  readonly incomplete = input(false);
  /** When set, the learner confirms with this message before the response is sent. */
  readonly confirm = input<string | null>(null);
  readonly check = output<void>();

  protected readonly confirming = signal<string | null>(null);

  protected ask(): void {
    const message = this.confirm();
    if (message) {
      this.confirming.set(message);
    } else {
      this.check.emit();
    }
  }

  protected send(): void {
    this.confirming.set(null);
    this.check.emit();
  }
}

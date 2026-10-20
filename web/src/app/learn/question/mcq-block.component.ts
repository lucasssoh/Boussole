import { ChangeDetectionStrategy, Component, computed, forwardRef, inject, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../../shared/rich-text.component';
import { BoussoleApi } from '../../core/boussole-api';
import { McqBlockView } from '../../core/api';
import { BlockListComponent } from '../blocks/block-list.component';
import { LearnFacade } from '../learn.facade';
import { SessionRun } from '../session-run';
import { QuestionFooterComponent } from './question-footer.component';
import { QuestionHeaderComponent } from './question-header.component';
import { QuestionState } from './question-state';

/** A multiple-choice question: radio buttons, or check boxes when several answers are right. */
@Component({
  selector: 'app-mcq-block',
  imports: [RichTextComponent, TranslocoPipe, QuestionFooterComponent, QuestionHeaderComponent, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let question = mcq();
    <article class="question" [class.solved]="state().progress().solved">
      <app-question-header [id]="question.id" [minutes]="question.minutes" [reused]="question.reused" [progress]="state().progress()" />
      <app-block-list [blocks]="question.statement" />
      @if (question.multiple) {
        <p class="note">{{ 'question.several' | transloco }}</p>
      }
      <ul class="choices">
        @for (choice of question.choices; track $index) {
          @let mark = state().item($index);
          @let expected = expectedChoice($index);
          <li [class.correct]="mark?.correct === true || expected?.correct" [class.wrong]="mark?.correct === false">
            <label>
              <input
                [type]="question.multiple ? 'checkbox' : 'radio'"
                [name]="question.id"
                [checked]="picked().has($index)"
                (change)="toggle($index)"
              />
              <app-rich-text class="choice-text" [text]="choice" />
            </label>
            @if (mark?.feedback ?? expected?.feedback; as feedback) {
              <p class="feedback"><app-rich-text [text]="feedback" /></p>
            }
          </li>
        }
      </ul>
      <app-question-footer [state]="state()" [ready]="picked().size > 0" (check)="check()" />
    </article>
  `,
})
export class McqBlockComponent {
  readonly mcq = input.required<McqBlockView>();

  private readonly api = inject(BoussoleApi);
  private readonly learn = inject(LearnFacade);
  private readonly run = inject(SessionRun, { optional: true });

  protected readonly state = computed(() => {
    const question = this.mcq();
    const create = () =>
      new QuestionState(this.api, this.learn.session()?.subject ?? '', question.id, question.progress, question.hints, this.run);
    return this.run?.question(question.id, create) ?? create();
  });
  protected readonly picked = computed(() => this.state().picked());

  /** Once the solution is open, every choice says whether it is right, with its feedback. */
  protected expectedChoice(index: number): { correct: boolean; feedback: string | null } | undefined {
    const expected = this.state().solution()?.expected;
    return expected?.kind === 'choices' ? expected.choices[index] : undefined;
  }

  protected toggle(index: number): void {
    const next = new Set(this.mcq().multiple ? this.picked() : []);
    if (next.has(index)) {
      next.delete(index);
    } else {
      next.add(index);
    }
    this.state().picked.set(next);
  }

  protected check(): void {
    void this.state().attempt({ kind: 'choices', picked: [...this.picked()] });
  }
}

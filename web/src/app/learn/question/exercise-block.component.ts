import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, forwardRef, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslocoPipe } from '@jsverse/transloco';
import { RichTextComponent } from '../../shared/rich-text.component';
import { AttemptRequest, ExerciseBlockView } from '../../core/api';
import { BoussoleApi } from '../../core/boussole-api';
import { BlockListComponent } from '../blocks/block-list.component';
import { LearnFacade } from '../learn.facade';
import { SessionRun } from '../session-run';
import { FillGaps } from './fill-gaps';
import { QuestionFooterComponent } from './question-footer.component';
import { QuestionHeaderComponent } from './question-header.component';
import { QuestionState } from './question-state';

type SelfGrade = 'got_it' | 'partly' | 'not_yet';

/**
 * An exercise: one field for a number or a text, fields in the statement for a fill exercise,
 * items to arrange or to pair, or, for a free exercise, a draft compared with the solution and
 * graded by the learner.
 */
@Component({
  selector: 'app-exercise-block',
  imports: [RichTextComponent, FormsModule, TranslocoPipe, QuestionFooterComponent, QuestionHeaderComponent, forwardRef(() => BlockListComponent)],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let question = exercise();
    @let input = question.input;
    @let expected = state().solution()?.expected;
    <article class="question" [class.solved]="state().progress().solved">
      <app-question-header [id]="question.id" [minutes]="question.minutes" [reused]="question.reused" [progress]="state().progress()" />
      <app-block-list [blocks]="question.statement" [fill]="fill()" />

      @switch (input.kind) {
        @case ('number') {
          <p class="answer-line" [class.correct]="verdictIs('correct')" [class.wrong]="verdictIs('wrong')">
            <input type="text" inputmode="decimal" [attr.aria-label]="'question.answer' | transloco" [ngModel]="typed()"
                   (ngModelChange)="type($event)" (keydown.enter)="check()" autocomplete="off" />
            @if (input.unit) {
              <span class="unit">{{ input.unit }}</span>
            }
          </p>
        }
        @case ('text') {
          <p class="answer-line" [class.correct]="verdictIs('correct')" [class.wrong]="verdictIs('wrong')">
            <input type="text" [attr.aria-label]="'question.answer' | transloco" [ngModel]="typed()"
                   (ngModelChange)="type($event)" (keydown.enter)="check()" autocomplete="off" spellcheck="false" />
          </p>
        }
        @case ('order') {
          <ol class="order-list">
            @for (item of order(); track item; let first = $first, last = $last, index = $index) {
              @let mark = state().item(index);
              <li [class.correct]="mark?.correct === true" [class.wrong]="mark?.correct === false">
                <span class="order-buttons">
                  <button type="button" class="quiet" [disabled]="first" [attr.aria-label]="'question.moveUp' | transloco" (click)="move(index, -1)">↑</button>
                  <button type="button" class="quiet" [disabled]="last" [attr.aria-label]="'question.moveDown' | transloco" (click)="move(index, 1)">↓</button>
                </span>
                <app-rich-text [text]="item" />
              </li>
            }
          </ol>
        }
        @case ('match') {
          <table class="match-table">
            <tbody>
              @for (left of input.lefts; track left) {
                @let mark = state().item(left);
                <tr [class.correct]="mark?.correct === true" [class.wrong]="mark?.correct === false">
                  <th scope="row"><app-rich-text [text]="left" /></th>
                  <td>
                    <select [attr.aria-label]="left" [ngModel]="pairs()[left] ?? ''" (ngModelChange)="pair(left, $event)">
                      <option value="">{{ 'question.choose' | transloco }}</option>
                      @for (right of input.rights; track right) {
                        <option [value]="right">{{ right }}</option>
                      }
                    </select>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
        @case ('free') {
          <textarea class="draft" rows="5" [placeholder]="'free.write' | transloco" [ngModel]="typed()" (ngModelChange)="type($event)"></textarea>
        }
        @case ('unreadable') {
          <p class="frame warning">{{ 'question.unreadable' | transloco }}</p>
        }
      }

      <app-question-footer [state]="state()" [checkable]="checkable()" [ready]="ready()" (check)="check()">
        <div expected>
          @switch (expected?.kind) {
            @case ('value') {
              @if (expected?.kind === 'value') {
                <p class="expected-line">{{ 'question.expected' | transloco }} : <strong><app-rich-text [text]="expected.value" /></strong></p>
              }
            }
            @case ('order') {
              @if (expected?.kind === 'order') {
                <ol class="expected-list">
                  @for (item of expected.items; track $index) {
                    <li><app-rich-text [text]="item" /></li>
                  }
                </ol>
              }
            }
            @case ('pairs') {
              @if (expected?.kind === 'pairs') {
                <ul class="expected-list">
                  @for (pair of expected.pairs; track $index) {
                    <li><app-rich-text [text]="pair[0]" /> → <app-rich-text [text]="pair[1]" /></li>
                  }
                </ul>
              }
            }
          }
        </div>
      </app-question-footer>

      @if (input.kind === 'free' && state().solution()) {
        <div class="self-grade">
          <span>{{ 'free.grade' | transloco }}</span>
          @for (grade of grades; track grade) {
            <button type="button" [class.primary]="graded() === grade" [disabled]="state().busy()" (click)="selfGrade(grade)">
              {{ 'free.' + grade | transloco }}
            </button>
          }
        </div>
      }
    </article>
  `,
})
export class ExerciseBlockComponent {
  readonly exercise = input.required<ExerciseBlockView>();

  private readonly api = inject(BoussoleApi);
  private readonly learn = inject(LearnFacade);
  private readonly run = inject(SessionRun, { optional: true });
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;

  protected readonly grades: SelfGrade[] = ['got_it', 'partly', 'not_yet'];
  protected readonly graded = signal<SelfGrade | null>(null);

  protected readonly state = computed(() => {
    const exercise = this.exercise();
    const create = () => {
      const state = new QuestionState(this.api, this.learn.session()?.subject ?? '', exercise.id, exercise.progress,
        exercise.hints, this.run, exercise.input.kind === 'free');
      state.order.set(exercise.input.kind === 'order' ? [...exercise.input.items] : []);
      return state;
    };
    return this.run?.question(exercise.id, create) ?? create();
  });
  protected readonly typed = computed(() => this.state().value());
  protected readonly order = computed(() => this.state().order());
  protected readonly pairs = computed(() => this.state().pairs());
  protected readonly fill = computed(() => {
    const input = this.exercise().input;
    return input.kind === 'fill' ? new FillGaps(input.gaps, this.state().gaps, () => this.check()) : null;
  });
  protected readonly checkable = computed(() => !['free', 'unreadable'].includes(this.exercise().input.kind));
  protected readonly ready = computed(() => {
    switch (this.exercise().input.kind) {
      case 'number':
      case 'text':
        return this.typed().trim() !== '';
      case 'fill':
        return this.fill()?.values() !== undefined && Object.values(this.fill()!.values()).some((value) => value.trim() !== '');
      case 'match':
        return Object.keys(this.pairs()).length > 0;
      default:
        return true;
    }
  });

  constructor() {
    effect(() => {
      const expected = this.state().solution()?.expected;
      this.fill()?.show(this.host, this.state().result()?.items ?? [], expected?.kind === 'gaps' ? expected.values : {});
    });
  }

  protected type(value: string): void {
    this.state().value.set(value);
  }

  protected verdictIs(verdict: string): boolean {
    return this.state().result()?.verdict === verdict;
  }

  protected move(index: number, step: number): void {
    const items = [...this.order()];
    [items[index], items[index + step]] = [items[index + step], items[index]];
    this.state().order.set(items);
  }

  protected pair(left: string, right: string): void {
    this.state().pairs.update((pairs) => {
      const next = { ...pairs };
      if (right) {
        next[left] = right;
      } else {
        delete next[left];
      }
      return next;
    });
  }

  protected check(): void {
    const request = this.request();
    if (request && this.ready()) {
      void this.state().attempt(request);
    }
  }

  protected selfGrade(grade: SelfGrade): void {
    this.graded.set(grade);
    void this.state().attempt({ kind: 'selfGrade', grade });
  }

  private request(): AttemptRequest | null {
    switch (this.exercise().input.kind) {
      case 'number':
      case 'text':
        return { kind: 'value', value: this.typed() };
      case 'fill':
        return { kind: 'gaps', values: this.fill()!.values() };
      case 'order':
        return { kind: 'order', items: this.order() };
      case 'match':
        return { kind: 'match', pairs: this.pairs() };
      default:
        return null;
    }
  }
}

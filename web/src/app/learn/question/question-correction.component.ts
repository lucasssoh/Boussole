import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, forwardRef, inject, input } from '@angular/core';
import { LanguageService } from '../../core/language.service';
import { formatPoints } from '../../shared/ranks';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockListComponent } from '../blocks/block-list.component';
import { QuestionState } from './question-state';

/**
 * The correction under a question, once it is answered: the verdict, what the answer was worth
 * and the points it earned,
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
        <span class="correction-score">
        @if (state().hook) {
          <span class="score-pill">{{ 'question.hook' | transloco }}</span>
        } @else {
          @if (result.possible > 1) {
            <span class="score-pill">{{ (result.earned === 1 ? 'question.sentence' : 'question.sentences') | transloco: { earned: result.earned, possible: result.possible } }}</span>
          } @else if (result.earned > 0 && result.earned < result.possible) {
            <span class="score-pill">{{ 'question.share' | transloco: { earned: points(result.earned) } }}</span>
          }
          @if (result.pointsGained > 0) {
            <span class="xp-pill">{{ 'question.points' | transloco: { points: points(result.pointsGained) } }}</span>
          }
        }
        </span>
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
  private readonly language = inject(LanguageService);
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;

  constructor() {
    effect(() => {
      const state = this.state();
      if (!state.correctionFocus()) {
        return;
      }
      requestAnimationFrame(() => {
        const bounds = this.host.getBoundingClientRect();
        if (bounds.top < 0 || bounds.bottom > window.innerHeight) {
          this.host.scrollIntoView({
            behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth',
            block: bounds.height > window.innerHeight ? 'start' : 'nearest',
          });
        }
        state.clearCorrectionFocus();
      });
    });
  }

  protected points(points: number): string {
    return formatPoints(points, this.language.current());
  }

  protected readonly verdict = computed(() => this.state().verdict());
}

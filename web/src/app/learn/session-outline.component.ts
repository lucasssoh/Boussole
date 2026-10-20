import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { Session, Verdict } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { LearnFacade } from './learn.facade';
import { SessionPlan, SessionScreen } from './session-screens';
import { SessionRun } from './session-run';

interface OutlineScreen {
  index: number;
  label: string;
  mark: 'current' | 'seen' | 'unseen' | Verdict;
}

/**
 * The plan of the session beside it: its parts, the screens of the current part, what has been
 * seen and how each question went. A click goes there.
 */
@Component({
  selector: 'app-session-outline',
  imports: [RouterLink, TranslocoPipe, RichTextComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'session-outline' },
  template: `
    @let current = session();
    <a class="outline-back" [routerLink]="['/subjects', current.subject]">← {{ learn.course()?.name }}</a>
    <h2 class="outline-session"><app-rich-text [text]="current.title" /></h2>
    <p class="outline-time">
      {{ (current.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: current.duration } }}
      · {{ 'session.spent' | transloco: { minutes: spentMinutes() } }}
    </p>
    <ol class="outline-parts">
      @for (part of parts(); track $index) {
        <li [class.current]="part.current" [class.done]="part.done">
          <button type="button" class="quiet outline-part" (click)="go.emit(part.first)">
            <span class="mark" aria-hidden="true">{{ part.done ? '✓' : part.current ? '◉' : '○' }}</span>
            <app-rich-text [text]="part.title" />
          </button>
          @if (part.current) {
            <ol class="outline-screens">
              @for (screen of part.screens; track screen.index) {
                <li [class]="screen.mark">
                  <button type="button" class="quiet" (click)="go.emit(screen.index)">
                    <span class="mark" aria-hidden="true">{{ symbol(screen.mark) }}</span>
                    <app-rich-text [text]="screen.label" />
                  </button>
                </li>
              }
            </ol>
          }
        </li>
      }
    </ol>
  `,
})
export class SessionOutlineComponent {
  readonly session = input.required<Session>();
  readonly plan = input.required<SessionPlan>();
  readonly current = input.required<number>();
  readonly go = output<number>();

  protected readonly learn = inject(LearnFacade);
  private readonly run = inject(SessionRun);
  private readonly transloco = inject(TranslocoService);

  protected readonly spentMinutes = computed(() => Math.floor(this.run.spentSeconds() / 60));
  protected readonly parts = computed(() => {
    const plan = this.plan();
    const current = this.current();
    const seen = this.run.seen();
    return plan.parts.map((part) => ({
      title: part.part.kind === 'notion' ? (part.part.title ?? '') : this.transloco.translate(`part.${part.part.kind}`),
      first: part.screens[0],
      current: part.screens.includes(current),
      done: part.screens.every((index) => seen.has(index)) && !part.screens.includes(current),
      screens: part.screens.map((index): OutlineScreen => ({
        index,
        label: this.label(plan.screens[index]),
        mark: this.mark(plan.screens[index], index, current, seen),
      })),
    }));
  });

  protected symbol(mark: OutlineScreen['mark']): string {
    return { current: '◉', seen: '✓', unseen: '○', correct: '✓', partly: '◐', wrong: '✗' }[mark];
  }

  private mark(screen: SessionScreen, index: number, current: number, seen: ReadonlySet<number>): OutlineScreen['mark'] {
    if (index === current) {
      return 'current';
    }
    const verdict = screen.question ? this.run.verdicts().get(screen.question.id) : undefined;
    return verdict ?? (seen.has(index) ? 'seen' : 'unseen');
  }

  private label(screen: SessionScreen): string {
    switch (screen.kind) {
      case 'question':
        return this.transloco.translate(screen.question?.type === 'mcq' ? 'screen.mcq' : 'screen.exercise');
      case 'example':
        return this.transloco.translate('screen.example');
      case 'explanation':
        return headingOf(screen) ?? this.transloco.translate('screen.explanation');
      default:
        return this.transloco.translate(`screen.${screen.kind}`);
    }
  }
}

/** An explanation that opens with a heading is named by it. */
function headingOf(screen: SessionScreen): string | undefined {
  const first = screen.blocks[0];
  return first?.type === 'prose' ? /^#{3,6}\s+(.+)$/m.exec(first.markdown.trimStart().split('\n')[0])?.[1] : undefined;
}

import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { Session, Verdict } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { LearnFacade } from './learn.facade';
import { ScreenKind, SessionPlan, SessionScreen } from './session-screens';
import { SessionRun } from './session-run';

type NodeState = 'current' | 'done' | 'open' | 'locked' | Verdict;

interface PathNode {
  index: number;
  kind: ScreenKind;
  label: string;
  state: NodeState;
  x: number;
  y: number;
}

interface PathHeading {
  title: string;
  y: number;
}

const WIDTH = 280;
const SWING = [0, 52, 70, 52, 0, -52, -70, -52];
const NODE_STEP = 96;
const HEADING_STEP = 56;
const TOP = 36;

/**
 * The session as a path beside it: a node per screen, winding down, grouped under the titles of
 * the parts. Seen screens turn green, questions take the colour of their verdict, screens the
 * learner cannot reach yet are locked. A click on an open node goes there.
 */
@Component({
  selector: 'app-session-path',
  imports: [RouterLink, TranslocoPipe, RichTextComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'session-path' },
  template: `
    @let current = session();
    <div class="path-head">
      <a class="path-back" [routerLink]="['/subjects', current.subject]">← {{ learn.course()?.name }}</a>
      <h2 class="path-title"><app-rich-text [text]="current.title" /></h2>
      <p class="path-time">
        {{ (current.kind === 'lesson' ? 'course.estimate' : 'course.timed') | transloco: { minutes: current.duration } }}
        · {{ 'session.spent' | transloco: { minutes: spentMinutes() } }}
      </p>
    </div>
    <div class="path-track" [style.height.px]="layout().height">
      <svg class="path-line" [attr.width]="width" [attr.height]="layout().height" aria-hidden="true">
        <path [attr.d]="layout().line"></path>
      </svg>
      @for (heading of layout().headings; track $index) {
        <div class="path-heading" [style.top.px]="heading.y"><app-rich-text [text]="heading.title" /></div>
      }
      @for (node of layout().nodes; track node.index) {
        <button
          type="button"
          class="path-node"
          [class]="'path-node ' + node.kind + ' ' + node.state"
          [style.left.px]="node.x"
          [style.top.px]="node.y"
          [disabled]="node.state === 'locked'"
          [attr.aria-current]="node.state === 'current' ? 'step' : null"
          [attr.aria-label]="node.label"
          (click)="go.emit(node.index)"
        >
          <span class="path-dot">
            @switch (node.state) {
              @case ('locked') {
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><rect x="5" y="11" width="14" height="9" rx="2"></rect><path d="M8 11V8a4 4 0 018 0v3"></path></svg>
              }
              @case ('correct') {
                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" aria-hidden="true"><path d="M5 12l5 5 9-10"></path></svg>
              }
              @case ('wrong') {
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"></path></svg>
              }
              @default {
                @switch (node.kind) {
                  @case ('start') {
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M6 21V4M6 4h11l-2 4 2 4H6"></path></svg>
                  }
                  @case ('explanation') {
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h10"></path></svg>
                  }
                  @case ('example') {
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M9 18h6M10 21h4M12 3a6 6 0 00-3.5 10.9V16h7v-2.1A6 6 0 0012 3z"></path></svg>
                  }
                  @case ('end') {
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M4 10h16v10H4zM3 6h18v4H3zM11 6h2v14h-2z"></path></svg>
                  }
                  @default {
                    <span class="path-q">?</span>
                  }
                }
              }
            }
          </span>
          <span class="path-label">{{ node.label }}</span>
        </button>
      }
    </div>
  `,
})
export class SessionPathComponent {
  readonly session = input.required<Session>();
  readonly plan = input.required<SessionPlan>();
  readonly current = input.required<number>();
  /** Screens after this one wait for an answer: they show locked. */
  readonly reachable = input.required<number>();
  readonly go = output<number>();

  protected readonly learn = inject(LearnFacade);
  protected readonly width = WIDTH;
  private readonly run = inject(SessionRun);
  private readonly transloco = inject(TranslocoService);
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;

  protected readonly spentMinutes = computed(() => Math.floor(this.run.spentSeconds() / 60));

  protected readonly layout = computed(() => {
    const plan = this.plan();
    const nodes: PathNode[] = [];
    const headings: PathHeading[] = [];
    let y = TOP;
    let lastPart = -2;
    plan.screens.forEach((screen, index) => {
      if (screen.part >= 0 && screen.part !== lastPart) {
        headings.push({ title: this.partTitle(plan, screen.part), y });
        y += HEADING_STEP;
      }
      lastPart = screen.part;
      nodes.push({
        index,
        kind: screen.kind,
        label: this.label(screen),
        state: this.state(screen, index),
        x: WIDTH / 2 + SWING[nodes.length % SWING.length],
        y,
      });
      y += NODE_STEP;
    });
    return { nodes, headings, height: y, line: curveThrough(nodes) };
  });

  constructor() {
    effect(() => {
      this.current();
      queueMicrotask(() => this.host.querySelector('.path-node.current')?.scrollIntoView({ block: 'center', behavior: 'smooth' }));
    });
  }

  private state(screen: SessionScreen, index: number): NodeState {
    if (index === this.current()) {
      return 'current';
    }
    if (index > this.reachable()) {
      return 'locked';
    }
    const verdict = screen.question ? this.run.verdicts().get(screen.question.id) : undefined;
    return verdict ?? (this.run.seen().has(index) ? 'done' : 'open');
  }

  private partTitle(plan: SessionPlan, part: number): string {
    const found = plan.parts[part].part;
    return found.kind === 'notion' ? (found.title ?? '') : this.transloco.translate(`part.${found.kind}`);
  }

  private label(screen: SessionScreen): string {
    const key = screen.kind === 'question' ? (screen.question?.type === 'mcq' ? 'mcq' : 'exercise') : screen.kind;
    return this.transloco.translate(`screen.${key}`);
  }
}

/** A smooth line through the centres of the nodes. */
function curveThrough(nodes: PathNode[]): string {
  if (nodes.length === 0) {
    return '';
  }
  const centre = (node: PathNode) => ({ x: node.x, y: node.y + 30 });
  let line = `M ${centre(nodes[0]).x} ${centre(nodes[0]).y}`;
  for (let index = 1; index < nodes.length; index++) {
    const from = centre(nodes[index - 1]);
    const to = centre(nodes[index]);
    const middle = (from.y + to.y) / 2;
    line += ` C ${from.x} ${middle}, ${to.x} ${middle}, ${to.x} ${to.y}`;
  }
  return line;
}

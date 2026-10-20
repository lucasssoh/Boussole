import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { Session, Verdict } from '../core/api';
import { RichTextComponent } from '../shared/rich-text.component';
import { LearnFacade } from './learn.facade';
import { ScreenKind, SessionPlan, SessionScreen } from './session-screens';
import { SessionRun } from './session-run';

type NodeState = 'current' | 'done' | 'open' | 'locked' | Verdict;
type ZoneKind = 'warmup' | 'notion' | 'notion-alt' | 'checkpoint' | 'practice' | 'text';

interface PathNode {
  index: number;
  kind: ScreenKind;
  label: string;
  state: NodeState;
  /** The centre of the node. */
  x: number;
  y: number;
}

interface PathZone {
  kind: ZoneKind;
  title: string;
  top: number;
  height: number;
  /** The centre of its sign, on the line. */
  signY: number;
}

const WIDTH = 288;
const CENTRE = WIDTH / 2;
/** Nodes zigzag on either side of the centre, so that the line winds evenly. */
const SWING = [-44, 44];
const NODE_STEP = 96;
const SIGN_STEP = 62;
const ZONE_GAP = 18;
const ZONE_INSET = 10;

/**
 * The session as a path beside it: a node per screen, winding down through a zone per part (warm-up,
 * each notion, checkpoint, practice), each opened by a sign on the line. Seen screens turn green, questions take the colour of their verdict, screens the
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
      <svg class="path-map" [attr.width]="width" [attr.height]="layout().height" aria-hidden="true">
        @for (zone of layout().zones; track $index) {
          <rect [attr.class]="'zone ' + zone.kind" [attr.x]="inset" [attr.y]="zone.top" [attr.width]="width - 2 * inset" [attr.height]="zone.height" rx="28"></rect>
        }
        <path class="path-line" [attr.d]="layout().line"></path>
      </svg>
      @for (zone of layout().zones; track $index) {
        <div [class]="'path-sign ' + zone.kind" [style.top.px]="zone.signY">
          @switch (zone.kind) {
            @case ('warmup') {
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><circle cx="12" cy="12" r="4"></circle><path d="M12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5L19 19M5 19l1.5-1.5M17.5 6.5L19 5"></path></svg>
            }
            @case ('checkpoint') {
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M6 21V4M6 4h11l-2 4 2 4H6"></path></svg>
            }
            @case ('practice') {
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M3 10v4M6 7v10M18 7v10M21 10v4M6 12h12"></path></svg>
            }
            @default {
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M4 5a2 2 0 012-2h13v16H6a2 2 0 00-2 2zM4 5v16"></path></svg>
            }
          }
          <app-rich-text [text]="zone.title" />
        </div>
      }
      @for (node of layout().nodes; track node.index) {
        <button
          type="button"
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
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><rect x="5" y="11" width="14" height="9" rx="2"></rect><path d="M8 11V8a4 4 0 018 0v3"></path></svg>
              }
              @case ('correct') {
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" aria-hidden="true"><path d="M5 12l5 5 9-10"></path></svg>
              }
              @case ('wrong') {
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"></path></svg>
              }
              @default {
                @switch (node.kind) {
                  @case ('start') {
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M6 21V4M6 4h11l-2 4 2 4H6"></path></svg>
                  }
                  @case ('explanation') {
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M4 6h16M4 12h16M4 18h10"></path></svg>
                  }
                  @case ('example') {
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M9 18h6M10 21h4M12 3a6 6 0 00-3.5 10.9V16h7v-2.1A6 6 0 0012 3z"></path></svg>
                  }
                  @case ('end') {
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M4 10h16v10H4zM3 6h18v4H3zM11 6h2v14h-2z"></path></svg>
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

  protected readonly inset = ZONE_INSET;

  /**
   * Everything is placed here, in one coordinate system: the zone of each part, its sign on the
   * line, a node per screen swinging left and right, and the line through all of them.
   */
  protected readonly layout = computed(() => {
    const plan = this.plan();
    const nodes: PathNode[] = [];
    const zones: PathZone[] = [];
    const stops: { x: number; y: number }[] = [];
    let y = 40;
    let swing = 0;
    let notions = 0;
    const place = (screen: SessionScreen, index: number, x: number) => {
      nodes.push({ index, kind: screen.kind, label: this.label(screen), state: this.state(screen, index), x, y });
      stops.push({ x, y });
      y += NODE_STEP;
    };
    plan.screens.forEach((screen, index) => {
      if (screen.part < 0) {
        place(screen, index, CENTRE);
        y += ZONE_GAP / 2;
        return;
      }
      const part = plan.parts[screen.part];
      if (part.screens[0] === index) {
        const kind = zoneKind(part.part.kind, notions);
        notions += part.part.kind === 'notion' ? 1 : 0;
        const top = y - SIGN_STEP / 2;
        zones.push({ kind, title: this.partTitle(plan, screen.part), top, height: 0, signY: y });
        stops.push({ x: CENTRE, y });
        y += SIGN_STEP;
        swing = 0;
      }
      place(screen, index, CENTRE + SWING[swing++ % SWING.length]);
      if (part.screens[part.screens.length - 1] === index) {
        const zone = zones[zones.length - 1];
        zone.height = y - NODE_STEP + 62 - zone.top;
        y += ZONE_GAP;
      }
    });
    return { nodes, zones, height: y, line: curveThrough(stops) };
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

/** A smooth line through the given points, top to bottom. */
function curveThrough(points: { x: number; y: number }[]): string {
  if (points.length === 0) {
    return '';
  }
  let line = `M ${points[0].x} ${points[0].y}`;
  for (let index = 1; index < points.length; index++) {
    const from = points[index - 1];
    const to = points[index];
    const middle = (from.y + to.y) / 2;
    line += ` C ${from.x} ${middle}, ${to.x} ${middle}, ${to.x} ${to.y}`;
  }
  return line;
}

/** The ambience of a part: notions alternate two tints, so that two notions in a row stay apart. */
function zoneKind(part: string, notionsBefore: number): ZoneKind {
  switch (part) {
    case 'warmup':
    case 'checkpoint':
    case 'practice':
      return part;
    case 'notion':
      return notionsBefore % 2 === 0 ? 'notion' : 'notion-alt';
    default:
      return 'text';
  }
}

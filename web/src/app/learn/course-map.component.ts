import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, afterNextRender, computed, effect, inject, input, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { TranslocoPipe } from '@jsverse/transloco';
import { Chapter, Course, SessionEntry } from '../core/api';
import { ServerSettingsService } from '../core/server-settings.service';
import { Link, layOut, reduceLinks } from '../shared/graph-layout';
import { chapterColor } from '../shared/chapter-color';
import { IconComponent } from '../shared/icon.component';
import { RichTextComponent } from '../shared/rich-text.component';

/** Where a node stands for the learner. */
type NodeState = 'done' | 'next' | 'open' | 'locked' | 'unwritten';

interface MapNode {
  id: string;
  /** The colour of its chapter. */
  tint: string;
  title: string;
  state: NodeState;
  /** Its floor: the nodes and links come in floor after floor, breadth first. */
  floor: number;
  x: number;
  y: number;
  width: number;
  height: number;
  kind?: string;
  done?: number;
  total?: number;
  waitingFor?: string;
}

interface MapLink {
  d: string;
  lit: boolean;
  tint: string;
  floor: number;
}

interface MapView {
  nodes: MapNode[];
  links: MapLink[];
  width: number;
  height: number;
}

/** The size of a block, and the room between floors; smaller on a narrow screen, where the mark sits above the title. */
const CHAPTER = { width: 240, height: 100, step: 152 };
const SESSION = { width: 220, height: 90, step: 136 };
const COMPACT_CHAPTER = { width: 150, height: 104, step: 146 };
const COMPACT_SESSION = { width: 140, height: 100, step: 140 };
const COMPACT_BELOW = 640;
const TOP = 16;
/** A block is never narrower; past that, the map scrolls sideways. */
const MIN_BLOCK = 170;
const COMPACT_MIN_BLOCK = 110;
/** The room between two blocks of a floor, and the room of a link passing through a floor. */
const GUTTER = 24;
const THROUGH_ROOM = 16;
const COMPACT_THROUGH_ROOM = 10;
/** The room a block wants on its floor: past what the width holds, blocks go to the floor below. */
const ROOM = 215;
const COMPACT_ROOM = 150;
const ZOOM_MS = 380;
const LEAVE_MS = 240;

/**
 * The map of a track: its chapters as blocks linked by what comes before what, a chapter waiting
 * for those one of its sessions needs; a chapter opens with a zoom on it into its sessions, linked
 * the same way; a track of one chapter opens on it. The blocks pop in floor after floor, each link drawn just before the block it
 * reaches. Finished blocks are checked, the next one stands out, those not open yet are locked.
 */
@Component({
  selector: 'app-course-map',
  imports: [IconComponent, RichTextComponent, RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'course-map', '[class.compact]': 'compact()' },
  template: `
    @if (openChapter(); as chapter) {
      <div class="map-head" [style.--tint]="tintOf(chapter.id)">
        @if (!single()) {
          <button type="button" class="back" [attr.aria-label]="'map.chapters' | transloco" [title]="'map.chapters' | transloco" (click)="close()"><app-icon name="arrow-left" /></button>
        }
        <h3><span class="map-dot" aria-hidden="true"></span><app-rich-text [text]="chapter.title" /></h3>
      </div>
    }
    <div class="map-scroll">
      @if (view(); as current) {
        <div class="map-canvas" [class.inside]="openChapter()" [style.--tint]="openChapter() ? tintOf(openChapter()!.id) : null" [class.zooming]="zooming()" [class.leaving]="leaving()" [style.width.px]="current.width" [style.height.px]="current.height"
          [style.transform-origin]="origin()">
          <svg class="map-links" [attr.width]="current.width" [attr.height]="current.height" aria-hidden="true">
            @for (link of current.links; track $index) {
              <path [attr.d]="link.d" [class.lit]="link.lit" [style.--i]="link.floor" [style.--tint]="link.tint" pathLength="1"></path>
            }
          </svg>
          @for (node of current.nodes; track node.id) {
            @if (openChapter()) {
              @if (node.state === 'unwritten') {
                <span class="map-node unwritten" [style.--i]="node.floor" [style.left.px]="node.x - node.width / 2" [style.top.px]="node.y - node.height / 2" [style.width.px]="node.width" [style.height.px]="node.height">
                  <span class="map-title"><app-rich-text [text]="node.title" /></span>
                  <span class="map-meta">{{ 'course.notWritten' | transloco }}</span>
                </span>
              } @else {
                <a [class]="'map-node session ' + node.state + ' ' + node.kind" [style.--i]="node.floor" [style.--tint]="node.tint"
                  [style.left.px]="node.x - node.width / 2" [style.top.px]="node.y - node.height / 2" [style.width.px]="node.width" [style.height.px]="node.height"
                  [routerLink]="['/subjects', course().id, 'sessions', node.id]">
                  <span class="map-mark" aria-hidden="true">
                    @switch (node.state) {
                      @case ('done') { <app-icon name="check" /> }
                      @case ('locked') { <app-icon name="lock" /> }
                      @default { <app-icon name="play" /> }
                    }
                  </span>
                  <span class="map-title"><app-rich-text [text]="node.title" /></span>
                  @if (node.kind !== 'lesson') {
                    <span class="map-meta">{{ 'kind.' + node.kind | transloco }}</span>
                  }
                  @if (node.waitingFor) {
                    <span class="visually-hidden">{{ afterKey() | transloco: { titles: node.waitingFor } }}</span>
                  }
                </a>
              }
            } @else {
              <button type="button" [class]="'map-node chapter ' + node.state" [style.--i]="node.floor" [style.--tint]="node.tint"
                [style.left.px]="node.x - node.width / 2" [style.top.px]="node.y - node.height / 2" [style.width.px]="node.width" [style.height.px]="node.height"
                (click)="open(node.id)">
                <span class="map-mark" aria-hidden="true">
                  @switch (node.state) {
                    @case ('done') { <app-icon name="check" /> }
                    @case ('locked') { <app-icon name="lock" /> }
                    @case ('unwritten') { }
                    @default { <app-icon name="play" /> }
                  }
                </span>
                <span class="map-title"><app-rich-text [text]="node.title" /></span>
                <span class="map-progress">
                  <span class="map-bar"><span [style.width.%]="(100 * (node.done ?? 0)) / (node.total || 1)"></span></span>
                  <span class="map-count">{{ node.done }} / {{ node.total }}</span>
                </span>
                @if (node.waitingFor) {
                  <span class="visually-hidden">{{ afterKey() | transloco: { titles: node.waitingFor } }}</span>
                }
              </button>
            }
          }
        </div>
      }
    </div>
  `,
})
export class CourseMapComponent {
  readonly course = input.required<Course>();
  readonly resume = input<string | null>(null);

  private readonly settings = inject(ServerSettingsService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;
  private readonly width = signal(960);
  protected readonly compact = computed(() => this.width() < COMPACT_BELOW);

  /** The chapter open, kept in the address so that coming back from a session finds it open. */
  private readonly chapterParam = toSignal(this.route.queryParamMap.pipe(map((params) => params.get('chapter'))), { initialValue: null });
  /** A track of one chapter has nothing to choose between: that chapter is open, for good. */
  protected readonly single = computed(() => this.course().chapters.length === 1);
  protected readonly openChapter = computed(() => {
    const chapters = this.course().chapters;
    return this.single() ? chapters[0] : (chapters.find((chapter) => chapter.id === this.chapterParam()) ?? null);
  });
  protected readonly zooming = signal<string | null>(null);
  protected readonly leaving = signal(false);

  protected readonly view = computed<MapView>(() => {
    const chapter = this.openChapter();
    return chapter ? this.sessionsView(chapter) : this.chaptersView();
  });

  /** The zoom starts from the block clicked. */
  protected readonly origin = computed(() => {
    const node = this.view().nodes.find((found) => found.id === this.zooming());
    return node ? `${node.x}px ${node.y}px` : 'center';
  });

  constructor() {
    const observer = new ResizeObserver(([entry]) => this.width.set(Math.max(320, Math.round(entry.contentRect.width))));
    afterNextRender(() => observer.observe(this.host));
    inject(DestroyRef).onDestroy(() => observer.disconnect());
    // the map opens on where the learner stands: the block to go on with, in the middle
    effect(() => {
      const next = this.view().nodes.find((node) => node.state === 'next');
      const scroller = this.host.querySelector<HTMLElement>('.map-scroll');
      if (next && scroller) {
        queueMicrotask(() => (scroller.scrollLeft = next.x - scroller.clientWidth / 2));
      }
    });
  }

  protected open(chapter: string): void {
    if (matchMedia('(prefers-reduced-motion: reduce)').matches) {
      this.show(chapter);
      return;
    }
    this.zooming.set(chapter);
    setTimeout(() => this.show(chapter), ZOOM_MS);
  }

  protected close(): void {
    const leave = () => {
      this.leaving.set(false);
      void this.router.navigate([], { relativeTo: this.route, queryParams: { chapter: null }, queryParamsHandling: 'merge', replaceUrl: true });
    };
    if (matchMedia('(prefers-reduced-motion: reduce)').matches) {
      leave();
      return;
    }
    this.leaving.set(true);
    setTimeout(leave, LEAVE_MS);
  }

  /** The colour of a chapter, by its place in the track. */
  protected tintOf(chapter: string): string {
    return chapterColor(this.course().chapters.findIndex((found) => found.id === chapter));
  }

  protected afterKey(): string {
    return this.settings.freeNavigation() ? 'course.advisedAfter' : 'course.after';
  }

  private show(chapter: string): void {
    this.zooming.set(null);
    void this.router.navigate([], { relativeTo: this.route, queryParams: { chapter }, queryParamsHandling: 'merge', replaceUrl: true });
  }

  private chaptersView(): MapView {
    const chapters = this.course().chapters;
    const ids = chapters.map((chapter) => chapter.id);
    const links: Link[] = chapters.flatMap((chapter) => chapter.after.map((before) => [before, chapter.id] as Link));
    const titles = new Map(chapters.map((chapter) => [chapter.id, chapter.title]));
    return this.place(ids, reduceLinks(ids, links), this.compact() ? COMPACT_CHAPTER : CHAPTER, (id) => {
      const chapter = chapters.find((found) => found.id === id)!;
      const written = chapter.sessions.filter((entry) => entry.written);
      const done = written.filter((entry) => entry.finished).length;
      const holds = chapter.sessions.some((entry) => entry.id === this.resume());
      const waiting = chapter.waitingFor.length > 0;
      const state: NodeState = written.length === 0 ? 'unwritten' : done === written.length ? 'done'
        : waiting && !this.settings.freeNavigation() ? 'locked' : holds ? 'next' : 'open';
      return { tint: this.tintOf(id), title: chapter.title, state, done, total: written.length,
        waitingFor: waiting ? chapter.waitingFor.map((wait) => titles.get(wait.id) ?? wait.title).join(', ') : undefined };
    }, (id) => this.chapterDone(chapters.find((found) => found.id === id)!), (id) => this.tintOf(id));
  }

  private sessionsView(chapter: Chapter): MapView {
    const ids = chapter.sessions.map((entry) => entry.id);
    const links: Link[] = chapter.sessions.flatMap((entry) => entry.after.filter((before) => ids.includes(before)).map((before) => [before, entry.id] as Link));
    const chapterWaits = chapter.waitingFor.map((wait) => wait.title);
    return this.place(ids, reduceLinks(ids, links), this.compact() ? COMPACT_SESSION : SESSION, (id) => {
      const entry = chapter.sessions.find((found) => found.id === id)!;
      const waits = [...chapterWaits, ...entry.waitingFor.map((prerequisite) => prerequisite.title)];
      return { tint: this.tintOf(chapter.id), title: entry.title, state: this.sessionState(entry, waits.length > 0), kind: entry.kind,
        waitingFor: !entry.finished && waits.length > 0 ? waits.join(', ') : undefined };
    }, (id) => chapter.sessions.find((found) => found.id === id)?.finished ?? false, () => this.tintOf(chapter.id));
  }

  private sessionState(entry: SessionEntry, waiting: boolean): NodeState {
    if (!entry.written) {
      return 'unwritten';
    }
    if (entry.finished) {
      return 'done';
    }
    if (waiting && !this.settings.freeNavigation()) {
      return 'locked';
    }
    return entry.id === this.resume() ? 'next' : 'open';
  }

  private chapterDone(chapter: Chapter): boolean {
    const written = chapter.sessions.filter((entry) => entry.written);
    return written.length > 0 && written.every((entry) => entry.finished);
  }

  /** The blocks on their floors, each floor spread over the width, and the links as curves leaving under a block and reaching the next from above. */
  private place(ids: string[], links: Link[], size: { width: number; height: number; step: number },
    describe: (id: string) => Omit<MapNode, 'id' | 'floor' | 'x' | 'y' | 'width' | 'height'>, finished: (id: string) => boolean,
    tint: (id: string) => string): MapView {
    const layout = layOut(ids, links, Math.max(2, Math.floor(this.width() / (this.compact() ? COMPACT_ROOM : ROOM))));
    // the blocks as wide as the fullest floor lets them, a link passing through a floor a narrow lane
    const compact = this.compact();
    const lane = compact ? COMPACT_THROUGH_ROOM : THROUGH_ROOM;
    const counts = layout.floors.map((slots) => ({ blocks: slots.filter((slot) => 'node' in slot).length, through: slots.filter((slot) => !('node' in slot)).length }));
    const fits = Math.min(...counts.filter((count) => count.blocks > 0).map((count) => (this.width() - count.through * lane) / count.blocks - GUTTER));
    const blockWidth = Math.round(Math.max(compact ? COMPACT_MIN_BLOCK : MIN_BLOCK, Math.min(size.width, fits)));
    const width = Math.max(this.width(), ...counts.map((count) => count.blocks * (blockWidth + GUTTER) + count.through * lane));
    const weight = (slot: (typeof layout.floors)[number][number]) => ('node' in slot ? 1 : lane / (blockWidth + GUTTER));
    const floorWeight = layout.floors.map((slots) => slots.reduce((sum, slot) => sum + weight(slot), 0));
    // each block as high as its whole title needs; each floor as high as its highest block
    const described = new Map(ids.map((id) => [id, describe(id)]));
    const heightOf = new Map(ids.map((id) => [id, this.blockHeight(described.get(id)!.title, blockWidth, size.height)]));
    const floorHeight = layout.floors.map((slots) => Math.max(size.height, ...slots.filter((slot) => 'node' in slot).map((slot) => heightOf.get((slot as { node: string }).node)!)));
    const gap = size.step - size.height;
    const floorTop = floorHeight.map((_, k) => TOP + floorHeight.slice(0, k).reduce((sum, h) => sum + h + gap, 0));
    const point = (floor: number, index: number) => {
      const before = layout.floors[floor].slice(0, index).reduce((sum, slot) => sum + weight(slot), 0);
      return {
        x: Math.round((width * (before + weight(layout.floors[floor][index]) / 2)) / floorWeight[floor]),
        y: floorTop[floor] + floorHeight[floor] / 2,
      };
    };
    const nodes: MapNode[] = [];
    layout.floors.forEach((slots, floor) => slots.forEach((slot, index) => {
      if ('node' in slot) {
        const { x, y } = point(floor, index);
        nodes.push({ id: slot.node, floor, x, y, width: blockWidth, height: heightOf.get(slot.node)!, ...described.get(slot.node)! });
      }
    }));
    // each link its own pin: those leaving a block spread under it, those reaching one over it
    const spread = (count: number, rank: number) => (count <= 1 ? 0 : -blockWidth * 0.22 + (blockWidth * 0.44 * rank) / (count - 1));
    const leaving = new Map<string, typeof layout.routes>();
    const reaching = new Map<string, typeof layout.routes>();
    layout.routes.forEach((route) => {
      leaving.set(route.link[0], [...(leaving.get(route.link[0]) ?? []), route]);
      reaching.set(route.link[1], [...(reaching.get(route.link[1]) ?? []), route]);
    });
    const firstX = (route: (typeof layout.routes)[number], at: number) => point(route.slots[at].floor, route.slots[at].index).x;
    leaving.forEach((routes) => routes.sort((a, b) => firstX(a, 1) - firstX(b, 1)));
    reaching.forEach((routes) => routes.sort((a, b) => firstX(a, a.slots.length - 2) - firstX(b, b.slots.length - 2)));
    const mapLinks: MapLink[] = layout.routes.map((route) => {
      const out = leaving.get(route.link[0])!;
      const into = reaching.get(route.link[1])!;
      const points = route.slots.map((slot) => point(slot.floor, slot.index));
      points[0] = { x: points[0].x + spread(out.length, out.indexOf(route)), y: points[0].y + heightOf.get(route.link[0])! / 2 };
      const last = points.length - 1;
      points[last] = { x: points[last].x + spread(into.length, into.indexOf(route)), y: points[last].y - heightOf.get(route.link[1])! / 2 };
      let d = `M${points[0].x} ${points[0].y}`;
      for (let k = 1; k < points.length; k++) {
        const bend = (points[k].y - points[k - 1].y) * 0.55;
        d += ` C${points[k - 1].x} ${points[k - 1].y + bend} ${points[k].x} ${points[k].y - bend} ${points[k].x} ${points[k].y}`;
      }
      return { d, lit: finished(route.link[0]), tint: tint(route.link[0]), floor: layout.floor.get(route.link[1]) ?? 0 };
    });
    const last = floorHeight.length - 1;
    return { nodes, links: mapLinks, width, height: last < 0 ? TOP * 2 : floorTop[last] + floorHeight[last] + TOP + 6 };
  }

  /** How high a block must be for its whole title: its lines measured in the font of the map, the rest of the block around them. */
  private blockHeight(title: string, blockWidth: number, base: number): number {
    const compact = this.compact();
    const fontSize = compact ? 12.5 : 14.7;
    const line = fontSize * 1.25;
    const room = compact ? blockWidth - 22 : blockWidth - 29 - 32 - 11;
    const context = (CourseMapComponent.measure ??= document.createElement('canvas').getContext('2d'));
    const text = title.replace(/\$|\*\*|`/g, '');
    let lines = 1;
    if (context) {
      context.font = `900 ${fontSize}px Nunito, system-ui, sans-serif`;
      let current = '';
      for (const word of text.split(/\s+/)) {
        const tried = current ? current + ' ' + word : word;
        if (current && context.measureText(tried).width > room) {
          lines++;
          current = word;
        } else {
          current = tried;
        }
      }
    } else {
      lines = Math.ceil((text.length * fontSize * 0.55) / room);
    }
    // the blocks were sized for two lines of title
    return Math.round(base + Math.max(0, lines - 2) * line);
  }

  private static measure: CanvasRenderingContext2D | null | undefined;
}

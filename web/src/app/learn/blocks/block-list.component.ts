import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockView } from '../../core/api';
import { FillGaps } from '../question/fill-gaps';
import { ExerciseBlockComponent } from '../question/exercise-block.component';
import { McqBlockComponent } from '../question/mcq-block.component';
import { RichTextComponent } from '../../shared/rich-text.component';
import { GraphBlockComponent } from './graph-block.component';
import { PdfBlockComponent } from './pdf-block.component';
import { ProseBlockComponent } from './prose-block.component';
import { VideoBlockComponent } from './video-block.component';

/**
 * Shows a list of blocks, each with the component of its type. The switch mirrors the sealed
 * Block of boussole-format: a new block type is one more case here. Blocks that only hold other
 * blocks (example, recall, callout) are drawn here, around a nested list.
 */
@Component({
  selector: 'app-block-list',
  imports: [ProseBlockComponent, VideoBlockComponent, PdfBlockComponent, GraphBlockComponent, McqBlockComponent, ExerciseBlockComponent, RichTextComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (block of blocks(); track $index) {
      @switch (block.type) {
        @case ('prose') {
          <app-prose-block [markdown]="block.markdown" [fill]="fill()" />
        }
        @case ('example') {
          <section class="frame example">
            <h4 class="frame-title">{{ 'block.example' | transloco }}@if (block.label) { — <app-rich-text [text]="block.label" />}</h4>
            <app-block-list [blocks]="block.blocks" />
          </section>
        }
        @case ('recall') {
          <aside class="frame recall">
            <h4 class="frame-title">{{ 'block.recall' | transloco }}</h4>
            <app-block-list [blocks]="block.blocks" />
          </aside>
        }
        @case ('callout') {
          <aside class="frame callout" [class]="block.kind">
            <h4 class="frame-title">{{ 'block.' + block.kind | transloco }}</h4>
            <app-block-list [blocks]="block.blocks" />
          </aside>
        }
        @case ('video') {
          <app-video-block [video]="block" />
        }
        @case ('pdf') {
          <app-pdf-block [pdf]="block" />
        }
        @case ('graph') {
          <app-graph-block [graph]="block" />
        }
        @case ('mcq') {
          <app-mcq-block [mcq]="block" />
        }
        @case ('exercise') {
          <app-exercise-block [exercise]="block" />
        }
        @case ('missingRef') {
          <p class="frame warning">{{ 'block.missingRef' | transloco: { id: block.id } }}</p>
        }
      }
    }
  `,
})
export class BlockListComponent {
  readonly blocks = input.required<BlockView[]>();
  /** The gaps of the fill exercise whose statement these blocks are, if any. */
  readonly fill = input<FillGaps | null>(null);
}

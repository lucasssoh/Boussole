import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { IconComponent } from '../shared/icon.component';
import { RichTextComponent } from '../shared/rich-text.component';
import { BlockListComponent } from './blocks/block-list.component';
import { PinnedLandmarks } from './landmarks';
import { LearnFacade } from './learn.facade';

/**
 * What stays in view beside the steps: the landmarks of the track this session uses, then those
 * the learner pinned, each of which they can take away.
 */
@Component({
  selector: 'app-landmarks-panel',
  imports: [BlockListComponent, IconComponent, RichTextComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'landmarks-panel' },
  template: `
    @for (landmark of fromTrack(); track landmark.id) {
      <section class="landmark-card">
        <h3 class="landmark-card-title"><app-rich-text [text]="landmark.title" /></h3>
        <app-block-list [blocks]="landmark.blocks" />
      </section>
    }
    @for (landmark of pinned.pinned(); track landmark.key) {
      <section class="landmark-card mine">
        <h3 class="landmark-card-title">
          <app-rich-text [text]="landmark.title" />
          <button type="button" class="quiet landmark-unpin" [title]="'landmarks.unpin' | transloco"
                  [attr.aria-label]="'landmarks.unpin' | transloco" (click)="pinned.unpin(landmark.key)">
            <app-icon name="close" [size]="16" />
          </button>
        </h3>
        <app-block-list [blocks]="landmark.blocks" />
      </section>
    }
    @if (fromTrack().length === 0 && pinned.pinned().length === 0) {
      <p class="landmarks-empty"><app-icon name="pin" /> {{ 'landmarks.empty' | transloco }}</p>
    }
  `,
})
export class LandmarksPanelComponent {
  private readonly learn = inject(LearnFacade);
  protected readonly pinned = inject(PinnedLandmarks);
  protected readonly fromTrack = computed(() => this.learn.session()?.landmarks ?? []);
}

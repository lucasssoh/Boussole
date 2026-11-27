import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { BlockView } from '../../core/api';
import { IconComponent } from '../../shared/icon.component';
import { PinnedLandmarks, landmarkKey } from '../landmarks';

/** Keeps a block in view beside the steps of the track, or takes it away. */
@Component({
  selector: 'app-pin-button',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="pin-button quiet" [class.pinned]="pinned()" [attr.aria-pressed]="pinned()"
            [title]="(pinned() ? 'landmarks.unpin' : 'landmarks.pin') | transloco"
            [attr.aria-label]="(pinned() ? 'landmarks.unpin' : 'landmarks.pin') | transloco"
            (click)="landmarks.toggle(title(), blocks())">
      <app-icon name="pin" [size]="16" />
    </button>
  `,
})
export class PinButtonComponent {
  readonly title = input.required<string>();
  readonly blocks = input.required<BlockView[]>();

  protected readonly landmarks = inject(PinnedLandmarks);
  protected readonly pinned = computed(() => this.landmarks.isPinned(landmarkKey(this.blocks())));
}

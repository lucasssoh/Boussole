import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { IconComponent } from '../../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { VideoBlockView } from '../../core/api';
import { LearnFacade } from '../learn.facade';

/** A video, opened beside the text so the learner can read and watch at once. */
@Component({
  selector: 'app-video-block',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="media-card" [class.active]="active()" (click)="open()">
      <img [src]="thumbnail()" alt="" loading="lazy" />
      <span class="media-text">
        <span class="media-action"><app-icon name="play" /> {{ 'block.watch' | transloco }}{{ segment() }}</span>
        <span>{{ video().label }}</span>
      </span>
    </button>
  `,
})
export class VideoBlockComponent {
  readonly video = input.required<VideoBlockView>();
  private readonly learn = inject(LearnFacade);

  protected readonly thumbnail = computed(() => `https://i.ytimg.com/vi/${this.video().youtubeId}/mqdefault.jpg`);
  protected readonly active = computed(() => {
    const panel = this.learn.panel();
    return panel?.kind === 'video' && panel.youtubeId === this.video().youtubeId;
  });
  protected readonly segment = computed(() => {
    const { start, end } = this.video();
    return start === null && end === null ? '' : ` · ${clock(start ?? 0)}${end === null ? '' : '–' + clock(end)}`;
  });

  protected open(): void {
    this.learn.panel.set({ kind: 'video', ...this.video() });
  }
}

export function clock(seconds: number): string {
  const minutes = Math.floor(seconds / 60);
  return `${minutes}:${String(seconds % 60).padStart(2, '0')}`;
}

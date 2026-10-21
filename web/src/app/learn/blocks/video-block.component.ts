import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { IconComponent } from '../../shared/icon.component';
import { TranslocoPipe } from '@jsverse/transloco';
import { VideoBlockView } from '../../core/api';
import { LearnFacade } from '../learn.facade';

/**
 * A video, opened beside the text so the learner can read and watch at once: from YouTube, or a
 * file of the sources (a lecture recording), whose first frame serves as its picture.
 */
@Component({
  selector: 'app-video-block',
  imports: [IconComponent, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button" class="media-card" [class.active]="active()" (click)="open()">
      @if (fileUrl(); as url) {
        <video class="media-picture" [src]="url + '#t=' + (video().start ?? 1)" preload="metadata" muted playsinline aria-hidden="true"></video>
      } @else {
        <img class="media-picture" [src]="thumbnail()" alt="" loading="lazy" />
      }
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
  protected readonly fileUrl = computed(() => {
    const path = this.video().path;
    return path === null ? null : this.learn.fileUrl(path);
  });
  protected readonly active = computed(() => {
    const panel = this.learn.panel();
    const video = this.video();
    return (panel?.kind === 'video' && panel.youtubeId === video.youtubeId)
      || (panel?.kind === 'localVideo' && panel.path === video.path);
  });
  protected readonly segment = computed(() => {
    const { start, end } = this.video();
    return start === null && end === null ? '' : ` · ${clock(start ?? 0)}${end === null ? '' : '–' + clock(end)}`;
  });

  protected open(): void {
    const { label, youtubeId, path, start, end } = this.video();
    this.learn.panel.set(path !== null
      ? { kind: 'localVideo', label, path, start, end }
      : { kind: 'video', label, youtubeId: youtubeId ?? '', start, end });
  }
}

export function clock(seconds: number): string {
  const minutes = Math.floor(seconds / 60);
  return `${minutes}:${String(seconds % 60).padStart(2, '0')}`;
}

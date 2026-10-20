import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { GraphBlockView } from '../../core/api';
import { drawGraph } from './graph-drawing';

let drawn = 0;

/**
 * A small graph of the course, drawn by Boussole: nodes as pills, edges as lines or arrows with
 * their label; what the author made stand out is in the colour of the course.
 */
@Component({
  selector: 'app-graph-block',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let drawing = picture();
    <figure class="graph">
      <svg [attr.viewBox]="'0 0 ' + drawing.width + ' ' + drawing.height" [attr.width]="drawing.width" role="img" [attr.aria-label]="graph().label || null">
        <defs>
          <marker [attr.id]="marker + '-plain'" viewBox="0 0 10 10" refX="9" refY="5" markerUnits="userSpaceOnUse" markerWidth="12" markerHeight="12" orient="auto-start-reverse">
            <path d="M0 0 L10 5 L0 10 z" class="graph-arrow" />
          </marker>
          <marker [attr.id]="marker + '-strong'" viewBox="0 0 10 10" refX="9" refY="5" markerUnits="userSpaceOnUse" markerWidth="12" markerHeight="12" orient="auto-start-reverse">
            <path d="M0 0 L10 5 L0 10 z" class="graph-arrow strong" />
          </marker>
        </defs>
        @for (line of drawing.edges; track $index) {
          <path class="graph-edge" [class.strong]="line.edge.emphasized" [attr.d]="line.path"
                [attr.marker-end]="line.edge.directed ? 'url(#' + marker + (line.edge.emphasized ? '-strong' : '-plain') + ')' : null" />
        }
        @for (line of drawing.edges; track $index) {
          @if (line.edge.label) {
            <g class="graph-edge-label" [class.strong]="line.edge.emphasized">
              <rect [attr.x]="line.labelX - line.labelWidth / 2" [attr.y]="line.labelY - 11" [attr.width]="line.labelWidth" height="22" rx="8" />
              <text [attr.x]="line.labelX" [attr.y]="line.labelY">{{ line.edge.label }}</text>
            </g>
          }
        }
        @for (place of drawing.nodes; track place.node.id) {
          <g class="graph-node" [class.strong]="place.node.emphasized">
            <rect [attr.x]="place.cx - place.width / 2" [attr.y]="place.cy - 20" [attr.width]="place.width" height="40" rx="20" />
            <text [attr.x]="place.cx" [attr.y]="place.cy">{{ place.node.text }}</text>
          </g>
        }
      </svg>
      @if (graph().label) {
        <figcaption>{{ graph().label }}</figcaption>
      }
    </figure>
  `,
})
export class GraphBlockComponent {
  readonly graph = input.required<GraphBlockView>();
  protected readonly picture = computed(() => drawGraph(this.graph()));
  /** Markers are found by ID: each graph of the page has its own. */
  protected readonly marker = `graph-${++drawn}`;
}

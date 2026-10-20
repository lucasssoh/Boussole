import { describe, expect, it } from 'vitest';
import { GraphBlockView } from '../../core/api';
import { drawGraph } from './graph-drawing';

const node = (id: string, x: number, y: number) => ({ id, text: id, x, y, emphasized: false });
const edge = (from: string, to: string) => ({ from, to, directed: true, label: '3', emphasized: false });

describe('drawGraph', () => {
  it('stops an edge at the border of its nodes', () => {
    const graph: GraphBlockView = { type: 'graph', label: '', nodes: [node('a', 0, 0), node('b', 1, 0)], edges: [edge('a', 'b')], width: 1, height: 0 };

    const drawing = drawGraph(graph);

    const [a, b] = drawing.nodes;
    expect(drawing.edges[0].path).toBe(`M ${a.cx + a.width / 2 + 3} ${a.cy} L ${b.cx - b.width / 2 - 3} ${b.cy}`);
    expect(drawing.edges[0].labelY).toBe(a.cy);
  });

  it('bends two opposite edges apart, so that both show', () => {
    const graph: GraphBlockView = { type: 'graph', label: '', nodes: [node('a', 0, 0), node('b', 1, 0)], edges: [edge('a', 'b'), edge('b', 'a')], width: 1, height: 0 };

    const [there, back] = drawGraph(graph).edges;

    expect(there.path).toContain(' Q ');
    expect(Math.sign(there.labelY - back.labelY)).not.toBe(0);
  });
});

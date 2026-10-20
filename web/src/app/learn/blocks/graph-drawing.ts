import { GraphBlockView, GraphEdgeView, GraphNodeView } from '../../core/api';

/** Pixels per grid unit, between two neighbouring nodes. */
const UNIT = 120;
const NODE_HEIGHT = 40;
const PADDING = 28;
/** Roughly the width of a character of a node's text, in pixels. */
const CHARACTER = 8.5;

export interface DrawnNode {
  node: GraphNodeView;
  cx: number;
  cy: number;
  width: number;
}

export interface DrawnEdge {
  edge: GraphEdgeView;
  /** An SVG path, from the border of one node to the border of the other. */
  path: string;
  labelX: number;
  labelY: number;
  labelWidth: number;
}

export interface GraphDrawing {
  width: number;
  height: number;
  nodes: DrawnNode[];
  edges: DrawnEdge[];
}

/**
 * Turns a graph into pixels: a node is a pill around its text; an edge stops at the borders of
 * its nodes, and bends when the opposite edge exists too, so that both stay visible.
 */
export function drawGraph(graph: GraphBlockView): GraphDrawing {
  const widest = Math.max(NODE_HEIGHT, ...graph.nodes.map((node) => nodeWidth(node)));
  const left = PADDING + widest / 2;
  const top = PADDING + NODE_HEIGHT / 2;
  const nodes = graph.nodes.map((node) => ({ node, cx: left + node.x * UNIT, cy: top + node.y * UNIT, width: nodeWidth(node) }));
  const byId = new Map(nodes.map((drawn) => [drawn.node.id, drawn]));
  const pairs = new Set(graph.edges.map((edge) => `${edge.from}\u0000${edge.to}`));
  const edges = graph.edges.map((edge) => {
    const from = byId.get(edge.from)!;
    const to = byId.get(edge.to)!;
    const bend = pairs.has(`${edge.to}\u0000${edge.from}`) ? 22 : 0;
    return drawEdge(edge, from, to, bend);
  });
  return {
    width: graph.width * UNIT + 2 * left,
    height: graph.height * UNIT + 2 * top,
    nodes,
    edges,
  };
}

function nodeWidth(node: GraphNodeView): number {
  return Math.max(NODE_HEIGHT, node.text.length * CHARACTER + 24);
}

function drawEdge(edge: GraphEdgeView, from: DrawnNode, to: DrawnNode, bend: number): DrawnEdge {
  const dx = to.cx - from.cx;
  const dy = to.cy - from.cy;
  const length = Math.hypot(dx, dy) || 1;
  // The normal, to bend an edge to its right when the opposite one exists.
  const nx = -dy / length;
  const ny = dx / length;
  const controlX = (from.cx + to.cx) / 2 + nx * bend * 2;
  const controlY = (from.cy + to.cy) / 2 + ny * bend * 2;
  const start = border(from, controlX - from.cx, controlY - from.cy);
  const end = border(to, controlX - to.cx, controlY - to.cy);
  const path = bend === 0
    ? `M ${start.x} ${start.y} L ${end.x} ${end.y}`
    : `M ${start.x} ${start.y} Q ${controlX} ${controlY} ${end.x} ${end.y}`;
  // The middle of the drawn line: a straight line's, or the point of the curve at its half.
  const labelX = bend === 0 ? (start.x + end.x) / 2 : (start.x + 2 * controlX + end.x) / 4;
  const labelY = bend === 0 ? (start.y + end.y) / 2 : (start.y + 2 * controlY + end.y) / 4;
  return { edge, path, labelX, labelY, labelWidth: (edge.label?.length ?? 0) * 7.5 + 12 };
}

/** Where a line leaving the centre of a node towards (dx, dy) crosses its border, a little outside. */
function border(node: DrawnNode, dx: number, dy: number): { x: number; y: number } {
  const halfWidth = node.width / 2 + 3;
  const halfHeight = NODE_HEIGHT / 2 + 3;
  const scale = 1 / Math.max(Math.abs(dx) / halfWidth, Math.abs(dy) / halfHeight, 1e-9);
  return { x: round(node.cx + dx * scale), y: round(node.cy + dy * scale) };
}

function round(value: number): number {
  return Math.round(value * 10) / 10;
}

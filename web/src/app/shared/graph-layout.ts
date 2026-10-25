/**
 * Lays out a small graph of what comes before what (chapters, or the sessions of a chapter) in
 * floors from the top: each node one floor below the lowest of those it waits for, a floor too
 * full giving nodes that can wait to the floor below, the order on
 * each floor the one with the fewest crossings, and every link that skips floors routed through a
 * gap of each floor it crosses. Pure functions, for the map of a track.
 */

/** A link: {@code from} comes before {@code to}. */
export type Link = readonly [string, string];

/** A place on a floor taken by a node, or by a link passing through. */
export type Slot = { node: string } | { through: Link };

export interface Layout {
  /** The floors from the top, each its slots from the left. */
  floors: Slot[][];
  /** The floor of each node: how many come before it, at most, one after another. */
  floor: Map<string, number>;
  /** Each link with the slots it goes through, from the node before to the node after. */
  routes: { link: Link; slots: { floor: number; index: number }[] }[];
  crossings: number;
}

/** The links once those another path already gives are left out. */
export function reduceLinks(nodes: readonly string[], links: readonly Link[]): Link[] {
  const after = new Map<string, string[]>(nodes.map((node) => [node, []]));
  links.forEach(([from, to]) => after.get(from)?.push(to));
  const reaches = (from: string, to: string, skip: Link): boolean => {
    const seen = new Set<string>();
    const stack = (after.get(from) ?? []).filter((next) => !(from === skip[0] && next === skip[1]));
    while (stack.length > 0) {
      const next = stack.pop()!;
      if (next === to) {
        return true;
      }
      if (!seen.has(next)) {
        seen.add(next);
        stack.push(...(after.get(next) ?? []));
      }
    }
    return false;
  };
  return links.filter((link) => !reaches(link[0], link[1], link));
}

/** How many links cross between two floors, in the order given. */
function crossingsBetween(upper: Slot[], lower: Slot[], segments: [Slot, Slot][]): number {
  const index = new Map<Slot, number>();
  upper.forEach((slot, i) => index.set(slot, i));
  lower.forEach((slot, i) => index.set(slot, i));
  const between = segments.filter(([a, b]) => upper.includes(a) && lower.includes(b));
  let count = 0;
  for (let i = 0; i < between.length; i++) {
    for (let j = i + 1; j < between.length; j++) {
      const [a, b] = between[i];
      const [c, d] = between[j];
      if ((index.get(a)! - index.get(c)!) * (index.get(b)! - index.get(d)!) < 0) {
        count++;
      }
    }
  }
  return count;
}

function permutations<T>(items: T[]): T[][] {
  if (items.length <= 1) {
    return [items];
  }
  return items.flatMap((item, i) => permutations([...items.slice(0, i), ...items.slice(i + 1)]).map((rest) => [item, ...rest]));
}

/** Every order tried while there are few; beyond, floor after floor by the mean place of what is linked above. */
const EXHAUSTIVE = 40_000;

export function layOut(nodes: readonly string[], links: readonly Link[], perFloor = Infinity): Layout {
  const before = new Map<string, string[]>(nodes.map((node) => [node, []]));
  links.forEach(([from, to]) => before.get(to)?.push(from));
  const floor = new Map<string, number>();
  const floorOf = (node: string, path: Set<string>): number => {
    if (floor.has(node)) {
      return floor.get(node)!;
    }
    path.add(node);
    const value = 1 + Math.max(-1, ...(before.get(node) ?? []).filter((up) => !path.has(up)).map((up) => floorOf(up, path)));
    path.delete(node);
    floor.set(node, value);
    return value;
  };
  nodes.forEach((node) => floorOf(node, new Set()));
  spreadDown(nodes, links, floor, perFloor);
  const count = Math.max(0, ...floor.values()) + 1;
  const floors: Slot[][] = Array.from({ length: nodes.length === 0 ? 0 : count }, () => []);
  const slotOf = new Map<string, Slot>();
  nodes.forEach((node) => {
    const slot: Slot = { node };
    slotOf.set(node, slot);
    floors[floor.get(node)!].push(slot);
  });
  const chains: { link: Link; slots: Slot[] }[] = links
    .filter(([from, to]) => floor.get(from)! < floor.get(to)!)
    .map((link) => {
      const slots: Slot[] = [slotOf.get(link[0])!];
      for (let k = floor.get(link[0])! + 1; k < floor.get(link[1])!; k++) {
        const through: Slot = { through: link };
        floors[k].push(through);
        slots.push(through);
      }
      slots.push(slotOf.get(link[1])!);
      return { link, slots };
    });
  const segments: [Slot, Slot][] = chains.flatMap((chain) => chain.slots.slice(1).map((slot, i) => [chain.slots[i], slot] as [Slot, Slot]));
  const total = (order: Slot[][]) => order.slice(1).reduce((sum, lower, i) => sum + crossingsBetween(order[i], lower, segments), 0);

  let best = floors.map((slots) => [...slots]);
  const tries = floors.reduce((product, slots) => product * factorial(slots.length), 1);
  if (tries <= EXHAUSTIVE) {
    let bestCount = Infinity;
    const search = (k: number, chosen: Slot[][]) => {
      if (bestCount === 0) {
        return;
      }
      if (k === floors.length) {
        const value = total(chosen);
        if (value < bestCount) {
          bestCount = value;
          best = chosen.map((slots) => [...slots]);
        }
        return;
      }
      for (const order of permutations(floors[k])) {
        search(k + 1, [...chosen, order]);
      }
    };
    search(0, []);
  } else {
    for (let sweep = 0; sweep < 4; sweep++) {
      for (let k = 1; k < best.length; k++) {
        const place = new Map(best[k - 1].map((slot, i) => [slot, i]));
        const mean = (slot: Slot) => {
          const ups = segments.filter(([, b]) => b === slot).map(([a]) => place.get(a) ?? 0);
          return ups.length === 0 ? Infinity : ups.reduce((a, b) => a + b, 0) / ups.length;
        };
        best[k] = [...best[k]].sort((a, b) => mean(a) - mean(b));
      }
    }
  }
  const at = new Map<Slot, { floor: number; index: number }>();
  best.forEach((slots, k) => slots.forEach((slot, index) => at.set(slot, { floor: k, index })));
  return {
    floors: best,
    floor,
    routes: chains.map((chain) => ({ link: chain.link, slots: chain.slots.map((slot) => at.get(slot)!) })),
    crossings: total(best),
  };
}

/**
 * At most {@code perFloor} nodes a floor: from the top, a floor too full gives a node to the next
 * floor, the one with the most room below it (the floor of its nearest successor), the last in
 * order among equals; with no room anywhere, its successors go down with it.
 */
function spreadDown(nodes: readonly string[], links: readonly Link[], floor: Map<string, number>, perFloor: number): void {
  if (!Number.isFinite(perFloor)) {
    return;
  }
  const after = new Map<string, string[]>(nodes.map((node) => [node, []]));
  links.forEach(([from, to]) => after.get(from)?.push(to));
  const lower = (node: string, to: number, seen: Set<string>) => {
    if (seen.has(node) || floor.get(node)! >= to) {
      return;
    }
    seen.add(node);
    floor.set(node, to);
    after.get(node)!.forEach((next) => lower(next, to + 1, seen));
  };
  for (let k = 0; k <= Math.max(0, ...floor.values()); k++) {
    let here = nodes.filter((node) => floor.get(node) === k);
    const room = (node: string) => Math.min(Infinity, ...after.get(node)!.map((next) => floor.get(next)!)) - k - 1;
    while (here.length > perFloor) {
      const chosen = here.reduce((best, node) => (room(node) >= room(best) ? node : best));
      lower(chosen, k + 1, new Set());
      here = here.filter((node) => node !== chosen);
    }
  }
}

function factorial(n: number): number {
  return n <= 1 ? 1 : n * factorial(n - 1);
}

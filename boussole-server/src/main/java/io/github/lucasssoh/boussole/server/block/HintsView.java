package io.github.lucasssoh.boussole.server.block;

import java.util.List;

/** How many hints a question has, and those already revealed. */
public record HintsView(int total, List<List<BlockView>> revealed) {
}

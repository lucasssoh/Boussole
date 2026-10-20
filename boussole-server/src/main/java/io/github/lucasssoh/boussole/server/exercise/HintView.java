package io.github.lucasssoh.boussole.server.exercise;

import io.github.lucasssoh.boussole.server.block.BlockView;
import io.github.lucasssoh.boussole.server.block.BlockViewMapper;
import io.github.lucasssoh.boussole.core.learn.RevealedHint;
import java.util.List;

record HintView(int number, int total, List<BlockView> blocks) {

    static HintView of(RevealedHint hint, BlockViewMapper mapper) {
        return new HintView(hint.number(), hint.total(), mapper.blocks(hint.hint().content()));
    }
}

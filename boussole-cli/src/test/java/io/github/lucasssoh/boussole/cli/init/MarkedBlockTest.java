package io.github.lucasssoh.boussole.cli.init;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MarkedBlockTest {

    private static final String BLOCK = MarkedBlock.START + "\nnew\n" + MarkedBlock.END + "\n";

    @Test
    void anEmptyFileGetsTheBlock() {
        assertThat(MarkedBlock.upsert(Optional.empty(), "new")).isEqualTo(BLOCK);
    }

    @Test
    void theBlockGoesAfterExistingText() {
        assertThat(MarkedBlock.upsert(Optional.of("mine"), "new")).isEqualTo("mine\n\n" + BLOCK);
    }

    @Test
    void anExistingBlockIsReplacedInPlace() {
        String file = "before\n" + MarkedBlock.START + "\nold\n" + MarkedBlock.END + "\nafter\n";

        assertThat(MarkedBlock.upsert(Optional.of(file), "new")).isEqualTo("before\n" + BLOCK + "after\n");
    }
}

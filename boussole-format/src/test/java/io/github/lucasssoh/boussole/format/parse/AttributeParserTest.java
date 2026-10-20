package io.github.lucasssoh.boussole.format.parse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AttributeParserTest {

    @Test
    void readsEveryForm() {
        Attributes attributes = AttributeParser.parse("#s11-a1 type=numeric title=\"avec des espaces\" multiple start='2:10'");

        assertThat(attributes.values()).isEqualTo(Map.of(
                "id", "s11-a1", "type", "numeric", "title", "avec des espaces", "multiple", "true", "start", "2:10"));
    }

    @Test
    void anEmptyTextHasNoAttribute() {
        assertThat(AttributeParser.parse("  ").isEmpty()).isTrue();
    }

    @Test
    void anAttributeIsGivenOnce() {
        assertThatThrownBy(() -> AttributeParser.parse("#a id=b")).hasMessage("`id` is given twice");
    }

    @Test
    void aQuoteIsClosed() {
        assertThatThrownBy(() -> AttributeParser.parse("title=\"ouvert")).hasMessage("`title` has an unclosed quote");
    }

    @Test
    void flagsAreTrueOrFalse() {
        assertThat(AttributeParser.parse("multiple").flag("multiple")).isTrue();
        assertThat(AttributeParser.parse("").flag("multiple")).isFalse();
        assertThatThrownBy(() -> AttributeParser.parse("multiple=oui").flag("multiple"))
                .hasMessage("`multiple` must be true or false, not `oui`");
    }
}

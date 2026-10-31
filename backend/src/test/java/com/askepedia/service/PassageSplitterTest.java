package com.askepedia.service;

import com.askepedia.entity.Passage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PassageSplitterTest {

    private PassageSplitter passageSplitter;

    @BeforeEach
    void setUp() {
        passageSplitter = new PassageSplitter();
    }

    @Test
    @DisplayName("split with empty or null string returns empty list")
    void split_emptyOrNull() {
        assertThat(passageSplitter.split(1L, null)).isEmpty();
        assertThat(passageSplitter.split(1L, "   ")).isEmpty();
    }

    @Test
    @DisplayName("split strips trailing References and See Also sections")
    void split_stripsNoiseSections() {
        String articleWithReferences = """
            Solar energy is radiant light and heat from the Sun that is harnessed using a range of technologies.
            It is an essential source of renewable energy.
            
            == References ==
            [1] Smith, J. (2020). Renewable energy today.
            [2] Doe, A. Solar systems worldwide.
            
            == See also ==
            * Wind power
            * Geothermal energy
            """;

        List<Passage> passages = passageSplitter.split(1L, articleWithReferences, 50, 500);

        assertThat(passages).isNotEmpty();
        for (Passage p : passages) {
            assertThat(p.passageText()).doesNotContain("== References ==");
            assertThat(p.passageText()).doesNotContain("Smith, J.");
            assertThat(p.passageText()).doesNotContain("Wind power");
        }
    }

    @Test
    @DisplayName("split preserves sequential 1-based sequenceNo")
    void split_sequenceNumbersAreSequential() {
        String text = """
            Paragraph one contains some detailed background about renewable systems and engineering principles.
            
            Paragraph two discusses photovoltaic cells, semiconductors, and how silicon crystals absorb photons.
            
            Paragraph three explores current grid integration, energy storage solutions, and battery technologies.
            """;

        List<Passage> passages = passageSplitter.split(42L, text, 50, 150);

        assertThat(passages).hasSizeGreaterThanOrEqualTo(2);
        for (int i = 0; i < passages.size(); i++) {
            assertThat(passages.get(i).sequenceNo()).isEqualTo(i + 1);
            assertThat(passages.get(i).articleId()).isEqualTo(42L);
        }
    }

    @Test
    @DisplayName("split splits excessively long paragraph on sentence boundaries")
    void split_splitsLongParagraphOnSentences() {
        String longParagraph = "Sentence one is informative. Sentence two adds more context. "
                + "Sentence three gives technical depth. Sentence four discusses implementation. "
                + "Sentence five concludes the point.";

        List<Passage> passages = passageSplitter.split(1L, longParagraph, 30, 80);

        assertThat(passages).hasSizeGreaterThan(1);
        for (Passage p : passages) {
            assertThat(p.passageText().length()).isLessThanOrEqualTo(85);
        }
    }
}

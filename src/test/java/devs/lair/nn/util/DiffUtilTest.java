package devs.lair.nn.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffUtilTest {

    @Test
    @DisplayName("Levenshtein size test")
    void levenshteinTest() {
        int i;

        i = DiffUtils.calculateLevenshtein("abc", "abd");
        assertThat(i).isEqualTo(1);

        i = DiffUtils.calculateLevenshtein("abc", "abda");
        assertThat(i).isEqualTo(2);

        i = DiffUtils.calculateLevenshtein("abc", "abcddd");
        assertThat(i).isEqualTo(3);
    }
}

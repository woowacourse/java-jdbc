package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @Nested
    class ExtractResultTest {

        @Test
        void nullableSingleResultEmptyException() {
            assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(Collections.emptyList()))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        void nullableSingleResultSizeException() {
            assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(List.of(1, 2)))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        void nullableSingleResultSize() {
            assertThat(DataAccessUtils.nullableSingleResult(List.of(1))).isEqualTo(1);
        }
    }
}

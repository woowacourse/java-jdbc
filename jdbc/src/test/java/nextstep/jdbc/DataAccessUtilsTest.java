package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @Test
    void singleResult() {
        String result = DataAccessUtils.singleResult(List.of("1"));

        assertThat(result).isEqualTo("1");
    }

    @Test
    void emptyList() {
        assertThatThrownBy(() -> DataAccessUtils.singleResult(Collections.emptyList()))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void twoValueList() {
        assertThatThrownBy(() -> DataAccessUtils.singleResult(List.of("1", "2")))
                .isInstanceOf(DataAccessException.class);
    }
}

package nextstep.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.dao.exception.EmptyResultDataAccessException;
import nextstep.dao.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @Test
    void nullableSingleResultWithEmpty() {
        assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(List.of()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void nullableSingleResultOver() {
        assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(List.of("lala", "skrr")))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void nullableSingleResult() {
        assertThat(DataAccessUtils.nullableSingleResult(List.of("lala"))).isEqualTo("lala");
    }
}

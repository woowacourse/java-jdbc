package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.jdbc.dao.EmptyResultDataAccessException;
import nextstep.jdbc.dao.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @DisplayName("요소가 0개인 경우 예외를 던진다.")
    @Test
    void 요소가_0개인_경우_예외를_던진다() {
        // given
        var numbers = List.of();

        // when & then
        assertThatThrownBy(() -> DataAccessUtils.singleResult(numbers))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("요소가 1개인 경우 해당 요소를 반환한다.")
    @Test
    void 요소가_1개인_경우_해당_요소를_반환한다() {
        // given
        var numbers = List.of(1);

        // when
        var actual = DataAccessUtils.singleResult(numbers);

        // then
        assertThat(actual).isEqualTo(1);
    }

    @DisplayName("요소가 1개를 초과하는 경우 예외를 던진다.")
    @Test
    void 요소가_1개를_초과하는_경우_예외를_던진다() {
        // given
        var numbers = List.of(1, 2);

        // when & then
        assertThatThrownBy(() -> DataAccessUtils.singleResult(numbers))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}

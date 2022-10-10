package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @Test
    @DisplayName("하나의 결과를 가져온다.")
    void getSingleResult() {
        List<String> result = List.of("test");

        final String singleResult = DataAccessUtils.getSingleResult(result);

        assertThat(singleResult).isEqualTo("test");
    }

    @Test
    @DisplayName("해당 컬렉션이 비어 있으면 예외 발생")
    void throwExceptionIsEmptyResult() {
        List<String> result = new ArrayList<>();

        assertThatThrownBy(() -> DataAccessUtils.getSingleResult(result))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("해당 컬렉션의 사이즈가 1보다 크면 예외 발생")
    void throwExceptionIsSizeOver() {
        List<String> result = List.of("seungpang", "klay");

        assertThatThrownBy(() -> DataAccessUtils.getSingleResult(result))
                .isInstanceOf(DataAccessException.class);
    }
}

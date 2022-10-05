package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @DisplayName("결과값이 존재하는 경우 해당 값이 반환된다.")
    @Test
    void nullableSingleResult() {
        List<Object> results = new ArrayList<>();
        results.add("object");

        Object result = DataAccessUtils.nullableSingleResult(results);
        assertThat(result).isEqualTo("object");
    }

    @DisplayName("결과값이 비어있을 경우 예외가 발생한다.")
    @Test
    void nullableSingleResultWithEmptyResult() {
        List<Object> results = new ArrayList<>();
        assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(results))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("결과값이 비어있습니다.");
    }

    @DisplayName("결과값이 2개 이상일 경우 예외가 발생한다.")
    @Test
    void nullableSingleResultWithMoreThanOneResult() {
        List<Object> results = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            results.add("object");
        }
        assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(results))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("잘못된 개수의 결과값이 반환되었습니다.");
    }
}

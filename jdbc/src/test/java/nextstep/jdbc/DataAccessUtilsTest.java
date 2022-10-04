package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.jdbc.exception.IllegalDataSizeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @DisplayName("데이터가 없는 경우에는 null을 반환한다.")
    @Test
    void emptyData() {
        // given
        final List<Object> emptyList = List.of();

        // when
        final Object result = DataAccessUtils.nullableSingleResult(emptyList);

        // then
        assertThat(result).isNull();
    }

    @DisplayName("데이터가 하나이상인 경우 예외가 발생한다.")
    @Test
    void multipleData() {
        // given
        final List<String> multipleData = List.of("one", "two");

        // when & then
        assertThatThrownBy(() -> DataAccessUtils.nullableSingleResult(multipleData))
                .isInstanceOf(IllegalDataSizeException.class);
    }

    @DisplayName("데이터가 하나인 경우 해당 데이터를 반환해준다.")
    @Test
    void oneData() {
        // given
        final List<String> data = List.of("data");

        // when
        final String result = DataAccessUtils.nullableSingleResult(data);

        // then
        assertThat(result).isEqualTo("data");
    }
}

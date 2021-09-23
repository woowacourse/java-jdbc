package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import exception.DataAccessException;
import exception.IncorrectDataSizeException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    @DisplayName("데이터가 null인 경우 DataAcessException을 던진다.")
    @Test
    void singleResultWhenNull() {
        assertThatThrownBy(() -> DataAccessUtils.singleResult(null))
            .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("데이터가 1개가 아닌 경우 IncorrectDataSizeException을 던진다.")
    @Test
    void singleResultWhenIncorrectDataSize() {
        assertThatThrownBy(() -> DataAccessUtils.singleResult(List.of()))
            .isInstanceOf(IncorrectDataSizeException.class);
    }

    @DisplayName("데이터가 1개인 경우 값을 반환한다.")
    @Test
    void singleResult() {
        String result = DataAccessUtils.singleResult(List.of("joanne"));
        assertThat(result).isEqualTo("joanne");
    }
}

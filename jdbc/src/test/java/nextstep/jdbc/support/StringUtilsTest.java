package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @DisplayName("문자열이 blank일 경우 예외를 던진다.")
    @Test
    void notBlank() {
        // given
        final var str = " ";

        // when & then
        assertThatThrownBy(() -> StringUtils.notBlank(str, "SQL"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

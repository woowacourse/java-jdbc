package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidUtilsTest {

    @DisplayName("객체가 null일 경우 예외를 던진다.")
    @Test
    void notNull() {
        // given
        final Object object = null;

        // when & then
        assertThatThrownBy(() -> ValidUtils.notNull(object, "Object"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("문자열이 blank일 경우 예외를 던진다.")
    @Test
    void notBlank() {
        // given
        final var str = " ";

        // when & then
        assertThatThrownBy(() -> ValidUtils.notBlank(str, "SQL"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

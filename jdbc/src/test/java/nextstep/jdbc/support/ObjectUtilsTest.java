package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ObjectUtilsTest {

    @DisplayName("객체가 null일 경우 예외를 던진다.")
    @Test
    void notNull() {
        // given
        final Object object = null;

        // when & then
        assertThatThrownBy(() -> ObjectUtils.notNull(object, "Object"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

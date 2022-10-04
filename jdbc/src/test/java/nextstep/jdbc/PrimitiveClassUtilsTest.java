package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PrimitiveClassUtilsTest {

    @Test
    void wrapPrimitiveClass() {
        assertThat(PrimitiveClassUtils.wrapPrimitiveClassIfNecessary(boolean.class)).isEqualTo(Boolean.class);
    }

    @Test
    void notWrap() {
        assertThat(PrimitiveClassUtils.wrapPrimitiveClassIfNecessary(Boolean.class)).isEqualTo(Boolean.class);
    }
}

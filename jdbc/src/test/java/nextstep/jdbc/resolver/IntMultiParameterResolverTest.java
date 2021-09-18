package nextstep.jdbc.resolver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntMultiParameterResolverTest {

    @DisplayName("")
    @Test
    void name() {
        //given
        IntMultiParameterResolver intMultiParameterResolver = new IntMultiParameterResolver();
        //when
        boolean support = intMultiParameterResolver.support(1);

        System.out.println(support);
        //then
    }
}
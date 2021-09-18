package nextstep.jdbc.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultiParameterResolversTest {

    @DisplayName("사용가능한 리졸버를 잘 찾는지 확인")
    @Test
    void findProperResolverTest() {
        //given
        MultiParameterResolvers multiParameterResolvers = new MultiParameterResolvers(
            new StringMultiParameterResolver(),
            new IntMultiParameterResolver()
        );
        //when
        MultiParameterResolver stringResolver = multiParameterResolvers.findProperResolver("치즈스트링");
        MultiParameterResolver intResolver = multiParameterResolvers.findProperResolver(1);
        //then
        assertThat(stringResolver).isInstanceOf(StringMultiParameterResolver.class);
        assertThat(intResolver).isInstanceOf(IntMultiParameterResolver.class);
    }
}
package nextstep.jdbc.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntMultiParameterResolverTest {

    @DisplayName("int타입으로 파라미터 리졸버에 true가 반환되는지 확인")
    @Test
    void supportTest() {
        //given
        IntMultiParameterResolver intMultiParameterResolver = new IntMultiParameterResolver();
        //when
        //then
        assertThat(intMultiParameterResolver.support(1)).isTrue();
    }

    @DisplayName("resolve 기능 테스트")
    @Test
    void resolveTest() throws SQLException {
        //given
        PreparedStatement mock = mock(PreparedStatement.class);
        //when
        IntMultiParameterResolver intMultiParameterResolver = new IntMultiParameterResolver();
        intMultiParameterResolver.resolve(mock,1, 5);
        //then
        verify(mock, atLeastOnce()).setInt(1, 5);
    }
}
package nextstep.jdbc.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringMultiParameterResolverTest {

    @DisplayName("String타입으로 파라미터 리졸버에 true가 반환되는지 확인")
    @Test
    void supportTest() {
        //given
        StringMultiParameterResolver stringMultiParameterResolver = new StringMultiParameterResolver();
        //when
        //then
        assertThat(stringMultiParameterResolver.support("우웨")).isTrue();
    }

    @DisplayName("resolve 기능 테스트")
    @Test
    void resolveTest() throws SQLException {
        //given
        PreparedStatement mock = mock(PreparedStatement.class);
        //when
        StringMultiParameterResolver stringMultiParameterResolver = new StringMultiParameterResolver();
        stringMultiParameterResolver.resolve(mock,1, "data");
        //then
        verify(mock, atLeastOnce()).setString(1, "data");
    }
}
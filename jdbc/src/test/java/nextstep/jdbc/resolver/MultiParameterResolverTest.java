package nextstep.jdbc.resolver;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultiParameterResolverTest {

    @DisplayName("resolve 기능 테스트")
    @Test
    void resolveTest() throws SQLException {
        //given
        PreparedStatement mock = mock(PreparedStatement.class);
        //when
        MultiParameterResolver multiParameterResolver = new MultiParameterResolver();

        Date now = new Date();
        multiParameterResolver.resolve(mock, 1, "data");
        multiParameterResolver.resolve(mock, 2, 2);
        multiParameterResolver.resolve(mock, 3, now);

        //then
        verify(mock, atLeastOnce()).setObject(1, "data");
        verify(mock, atLeastOnce()).setObject(2, 2);
        verify(mock, atLeastOnce()).setObject(3, now);
    }
}
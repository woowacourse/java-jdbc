package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArgumentPreparedStatementSetterTest {

    private PreparedStatement preparedStatement;
    private ArgumentPreparedStatementSetter argumentPreparedStatementSetter;

    @BeforeEach
    void setUp() {
        preparedStatement = mock(PreparedStatement.class);
    }

    @DisplayName("setValues() 함수를 통해 preparedStatement.setObject()가 정상동작 되는지 확인")
    @Test
    void setValues() throws SQLException {
        //given
        Object[] values = new Object[]{"ing", "1234", "ing@woowahan.com"};
        argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(values);

        //when
        argumentPreparedStatementSetter.setValues(preparedStatement);

        //then
        verify(preparedStatement, times(3)).setObject(anyInt(), any());
    }
}

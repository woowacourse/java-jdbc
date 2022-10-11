package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.element.PreparedStatementSetter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultStatementSetterTest {

    @DisplayName("getSetter 에서 string, number 외의 래퍼 클래스를 세팅할 수 없다.")
    @Test
    void getStatementSetter_Exception_Wrapper() throws SQLException {
        //given
        Connection connection = spy(Connection.class);
        final var sql = "update users set account = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        DefaultStatementSetter statementSetter = new DefaultStatementSetter();

        PreparedStatementSetter preparedStatementSetter = statementSetter.getSetter(new Wrapper("hunch"));
        assertThatThrownBy(() -> preparedStatementSetter.setValues(statement))
                .isInstanceOf(SQLException.class);
    }

    @DisplayName("getSetter 에서 null 세팅할 수 없다.")
    @Test
    void getStatementSetter_Exception_Null() throws SQLException {
        //given
        Connection connection = spy(Connection.class);
        final var sql = "update users set account = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        DefaultStatementSetter statementSetter = new DefaultStatementSetter();

        PreparedStatementSetter preparedStatementSetter = statementSetter.getSetter(null);
        assertThatThrownBy(() -> preparedStatementSetter.setValues(statement))
                .isInstanceOf(SQLException.class);
    }
}

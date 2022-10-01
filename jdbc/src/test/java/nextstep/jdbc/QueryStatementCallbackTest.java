package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

class QueryStatementCallbackTest {

    private StatementCallback statementCallback;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.statementCallback = new QueryStatementCallback(preparedStatement);

        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(preparedStatement.executeQuery(anyString())).willReturn(resultSet);
    }

    @Test
    void setPreparedStatement() throws SQLException {
        statementCallback.setPreparedSql(1, 2, 3);
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 2);
        verify(preparedStatement).setObject(3, 3);
    }

    @Test
    void doInStatement() throws SQLException {
        RowMapperResultSetExtractor rowMapperResultSetExtractor = mock(RowMapperResultSetExtractor.class);
        given(rowMapperResultSetExtractor.extractData(resultSet)).willReturn(List.of("A"));
        statementCallback.doInStatement(rowMapperResultSetExtractor);

        verify(resultSet).close();
    }
}

package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class QueryStatementCallback extends StatementCallback {

    public QueryStatementCallback(final PreparedStatement pstmt) {
        super(pstmt);
    }

    public <T> T doInStatement(final ResultSetExtractor<T> resultSetExtractor) {
        try (ResultSet resultSet = getPstmt().executeQuery()) {
            return resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}

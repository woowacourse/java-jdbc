package nextstep.jdbc;

import org.springframework.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<R> {
    R doInStatement(final PreparedStatement preparedStatement) throws SQLException, DataAccessException;
}

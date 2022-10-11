package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

@FunctionalInterface
public interface PreparedStatementExecutor<T> {

    T doInPreparedStatement(final PreparedStatement preparedStatement) throws SQLException, DataAccessException;
}

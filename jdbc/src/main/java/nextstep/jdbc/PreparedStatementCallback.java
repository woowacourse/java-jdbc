package nextstep.jdbc;

import org.springframework.dao.DataAccessException;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T, R> {
    R doInStatement(T t) throws SQLException, DataAccessException;
}

package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryExecutor<T> {

    T executePreparedStatement(PreparedStatement ps) throws SQLException;
}

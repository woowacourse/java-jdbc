package nextstep.jdbc;

import java.sql.SQLException;

public interface QueryExecutor<T> {

    T executePreparedStatement(PreparedStatementStarter pss) throws SQLException;
}

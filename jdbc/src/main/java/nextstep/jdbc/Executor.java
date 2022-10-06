package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

interface Executor<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;
}

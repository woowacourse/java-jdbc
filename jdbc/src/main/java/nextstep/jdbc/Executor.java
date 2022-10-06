package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Executor<T> {

    T execute(PreparedStatement statement) throws SQLException;
}

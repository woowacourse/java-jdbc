package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementExecutor<T> {
    @Nullable
    T execute(PreparedStatement preparedStatement) throws SQLException;
}

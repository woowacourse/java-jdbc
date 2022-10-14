package nextstep.jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface StatementExecutor<T> {

    T execute(final PreparedStatement preparedStatement);
}

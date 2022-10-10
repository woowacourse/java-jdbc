package nextstep.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlResultProcessor<T, K> {

    T process(K sqlResult) throws SQLException;
}

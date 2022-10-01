package nextstep.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetFunction<T, R> {

    R apply(T t) throws SQLException;
}

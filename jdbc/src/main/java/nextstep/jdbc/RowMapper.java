package nextstep.jdbc;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T run(final ResultSet rs);
}

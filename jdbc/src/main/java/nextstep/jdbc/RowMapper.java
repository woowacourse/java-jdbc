package nextstep.jdbc;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(final ResultSet rs, int rowNum);
}

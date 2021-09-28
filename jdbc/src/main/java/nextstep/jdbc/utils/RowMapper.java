package nextstep.jdbc.utils;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T mapRow(ResultSet rs, int rowNum);
}

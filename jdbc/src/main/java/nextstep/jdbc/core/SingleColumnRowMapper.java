package nextstep.jdbc.core;

import nextstep.jdbc.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<?> columnType;

    public SingleColumnRowMapper(Class<T> columnType) {
        this.columnType = columnType;
    }

    @Override
    public T mapRow(ResultSet rs) throws SQLException {
        return (T) JdbcUtils.getSingleResultSetValue(rs, columnType);
    }
}

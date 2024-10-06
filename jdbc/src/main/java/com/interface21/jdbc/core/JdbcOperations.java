package com.interface21.jdbc.core;

import java.util.List;

public interface JdbcOperations {

    <T> List<T> query(String sql, RowMapper<T> rowMapper);

    <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values);

    <T> T queryForObject(String sql, Class<T> clazz, Object... values);

    void update(String sql, Object... values);

    void execute(String sql);
}

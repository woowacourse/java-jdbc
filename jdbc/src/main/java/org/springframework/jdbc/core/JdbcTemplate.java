package org.springframework.jdbc.core;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.mapper.ResultSetObjectMapper;

public class JdbcTemplate extends JdbcTemplateExecutionBase {

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public <T> T executeQueryForObject(final String sql, final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectBase(sql, mapper, new Object[]{});
    }

    public <T> T executeQueryForObject(final String sql,
                                       final ResultSetObjectMapper<T> mapper,
                                       final Object... params) {
        return super.executeQueryForObjectBase(sql, mapper, params);
    }

    public <T> List<T> executeQueryForObjects(final String sql, final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectsBase(sql, mapper, new Object[]{});
    }

    public <T> List<T> executeQueryForObjects(final String sql,
                                              final ResultSetObjectMapper<T> mapper,
                                              final Object... params) {
        return super.executeQueryForObjectsBase(sql, mapper, params);
    }

    public void update(final String sql) {
        updateBase(sql, new Object[]{});
    }

    public void update(final String sql, Object... params) {
        updateBase(sql, params);
    }
}

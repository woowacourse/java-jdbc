package org.springframework.jdbc.core;

import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate extends JdbcTemplateExecutionBase {

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public <T> T executeQueryForObject(final String sql, final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectBase(sql, mapper, new Object[]{}, TRANSACTION_ENABLE);
    }

    public <T> T executeQueryForObject(final String sql,
                                       final ResultSetObjectMapper<T> mapper,
                                       final Object... params) {
        return super.executeQueryForObjectBase(sql, mapper, params, TRANSACTION_ENABLE);
    }

    public <T> T executeQueryForObjectWithoutTransaction(final String sql, final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectBase(sql, mapper, new Object[]{}, TRANSACTION_ENABLE);
    }

    public <T> T executeQueryForObjectWithoutTransaction(final String sql,
                                                         final ResultSetObjectMapper<T> mapper,
                                                         final Object... params) {
        return super.executeQueryForObjectBase(sql, mapper, params, TRANSACTION_DISABLE);
    }

    public <T> List<T> executeQueryForObjects(final String sql, final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectsBase(sql, mapper, new Object[]{}, TRANSACTION_ENABLE);
    }

    public <T> List<T> executeQueryForObjects(final String sql,
                                              final ResultSetObjectMapper<T> mapper,
                                              final Object... params) {
        return super.executeQueryForObjectsBase(sql, mapper, params, TRANSACTION_ENABLE);
    }

    public <T> List<T> executeQueryForObjectsWithoutTransaction(final String sql,
                                                                final ResultSetObjectMapper<T> mapper) {
        return super.executeQueryForObjectsBase(sql, mapper, new Object[]{}, TRANSACTION_DISABLE);
    }

    public <T> List<T> executeQueryForObjectsWithoutTransaction(final String sql,
                                                                final ResultSetObjectMapper<T> mapper,
                                                                final Object... params) {
        return super.executeQueryForObjectsBase(sql, mapper, params, TRANSACTION_DISABLE);
    }

    public void update(final String sql) {
        updateBase(sql, new Object[]{}, TRANSACTION_ENABLE);
    }

    public void update(final String sql, Object... params) {
        updateBase(sql, params, TRANSACTION_ENABLE);
    }

    public void updateWithoutTransaction(final String sql) {
        updateBase(sql, new Object[]{}, TRANSACTION_DISABLE);
    }

    public void updateWithoutTransaction(final String sql, Object... params) {
        updateBase(sql, params, TRANSACTION_DISABLE);
    }
}

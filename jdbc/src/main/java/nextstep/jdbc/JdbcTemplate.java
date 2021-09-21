package nextstep.jdbc;

import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... parameters) {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetterImpl(parameters);
        AbstractJdbcTemplate abstractJdbcTemplate = new AbstractJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

        };
        abstractJdbcTemplate.update(preparedStatementSetter);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ... parameters) {
        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetterImpl(parameters);
        AbstractJdbcTemplate abstractJdbcTemplate = new AbstractJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

        };
        return abstractJdbcTemplate.queryForObject(preparedStatementSetter, rowMapper);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        AbstractJdbcTemplate abstractJdbcTemplate = new AbstractJdbcTemplate() {
            @Override
            protected String createQuery() {
                return sql;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

        };
        return abstractJdbcTemplate.query(rowMapper);
    }
}

package nextstep.jdbc;

import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final Connector connector;

    public JdbcTemplate(final DataSource dataSource) {
        this.connector = new JdbcConnector(dataSource);
    }

    public int update(final String sql, final Object... parameters) {
        log.debug("query : {}", sql);
        return connector.execute(sql, new UpdateExecutor(), parameters);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object... parameters) {
        log.debug("query : {}", sql);
        return forObject(connector.execute(sql, new FindExecutor<>(rowMapper), parameters));
    }

    public <T> List<T> queryForList(final String sql, RowMapper<T> rowMapper, final Object... parameters) {
        log.debug("query : {}", sql);
        return connector.execute(sql,new FindExecutor<>(rowMapper), parameters);
    }

    private <T> T forObject(final List<T> ts) {
        return DataAccessUtils.nullableSingleResult(ts);
    }
}

package nextstep.jdbc;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    protected static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected DataSource datasource;

    public JdbcTemplate(DataSource datasource) {
        this.datasource = datasource;
    }
}

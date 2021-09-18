package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.connector.DbConnector;
import nextstep.jdbc.connector.DbConnectorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DbConnector dbConnector;

    public JdbcTemplate(DataSource dataSource) {
        this.dbConnector = new DbConnectorImpl(dataSource);
    }


}

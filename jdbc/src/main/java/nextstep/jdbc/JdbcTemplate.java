package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.connector.DbConnector;
import nextstep.jdbc.connector.DbConnectorImpl;
import nextstep.jdbc.resolver.PstmtParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DbConnector dbConnector;

    public JdbcTemplate(DataSource dataSource) {
        this.dbConnector = new DbConnectorImpl(dataSource);
    }

    public QueryExecuteResult executeInsertOrUpdate(String sql, PstmtParameterResolver resolver) {
        return execute(sql, resolver);
    }

    private QueryExecuteResult execute(String sql, PstmtParameterResolver resolver) {
        try (Connection connection = dbConnector.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            resolver.resolve(pstmt);
            return new QueryExecuteResult(pstmt.executeUpdate());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("DML 처리 중 오류가 발생했습니다.", e);
        }
    }
}

package nextstep.jdbc;

import nextstep.exception.connectioon.ConnectionAcquisitionFailureException;
import nextstep.exception.connectioon.ConnectionCloseFailureException;
import nextstep.exception.statement.StatementExecutionFailureException;
import nextstep.exception.statement.StatementInitializationFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, String... args) {
        try (
                final Connection conn = getConnection();
                final PreparedStatement pstmt = createPreparedStatement(conn, sql, args)
        ) {
            log.debug("query : {}", sql);
            executePreparedStatement(pstmt);

        } catch (SQLException e) {
            throw new ConnectionCloseFailureException(e.getMessage());
        }
    }

    // TODO : 잘 닫히나 확인하기
    private PreparedStatement createPreparedStatement(Connection conn, String sql, String[] args) {
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            int i = 1;
            for (String arg : args) {
                preparedStatement.setString(i++, arg);
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new StatementInitializationFailureException(e.getMessage());
        }
    }

    private void executePreparedStatement(PreparedStatement pstmt) {
        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new StatementExecutionFailureException(e.getMessage());
        }
    }

    // TODO : 잘 닫히나 확인하기
    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionAcquisitionFailureException(e.getMessage());
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

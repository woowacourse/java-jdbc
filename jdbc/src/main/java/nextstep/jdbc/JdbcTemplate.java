package nextstep.jdbc;

import nextstep.jdbc.callback.StatementCallback;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.setter.ArgumentPreparedStatementSetter;
import nextstep.jdbc.setter.PreparedStatementSetter;
import nextstep.jdbc.setter.SimplePreparedStatementSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    //todo 기본 생성자로 설정파일에 정의된 dataSource를 가져올 수 없을까?
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql) {
        executeUpdate(connection -> connection.prepareStatement(sql), new SimplePreparedStatementSetter());
    }

    public void update(String sql, Object... args) {
        executeUpdate(connection -> connection.prepareStatement(sql), new ArgumentPreparedStatementSetter(args));
    }

    public void executeUpdate(StatementCallback statementCallback, PreparedStatementSetter preparedStatementSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = statementCallback.makePrepareStatement(conn)) {
            preparedStatementSetter.setValues(pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("JdbcTemplate Database Access Failed", e);
            throw new DataAccessException("JdbcTemplate Database Access Failed");
        }
    }
}

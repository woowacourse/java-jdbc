package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(PreparedStatementCreator preparedStatementCreator) {
        return execute(preparedStatementCreator, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object ... args) {
        List<T> result = query((connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            int index = 1;
            for (Object arg : args) {
                pstmt.setObject(index++, arg);
            }
            return pstmt;
        }), new ResultSetExtractor<>(rowMapper));
        return result.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query((connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            return pstmt;
        }), new ResultSetExtractor<>(rowMapper));
    }

    public <T> List<T> query(PreparedStatementCreator preparedStatementCreator,
                             ResultSetExtractor<T> resultSetExtractor) {
        return execute(preparedStatementCreator, (pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()){
                return resultSetExtractor.extract(rs);
            }
        }));
    }

    public <T> T execute(PreparedStatementCreator preparedStatementCreator,
                         PreparedStatementCallback<T> preparedStatementCallback) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = preparedStatementCreator.createPreparedStatement(conn))
        {
            return preparedStatementCallback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

}

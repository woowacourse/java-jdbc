package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> selectQuery(String sql, JdbcMapper<T> jdbcMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = injectParams(conn.prepareStatement(sql), params);
             ResultSet resultSet = pstmt.executeQuery()) {

            log.debug("query : {}", sql);
            return jdbcMapper.mapRow(resultSet);

        } catch (SQLException | DataAccessException | InvocationTargetException | NoSuchMethodException |
                 InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void nonSelectQuery(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = injectParams(conn.prepareStatement(sql), params)) {

            log.debug("query : {}", sql);
            pstmt.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement injectParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        int totalParamCount = parameterMetaData.getParameterCount();
        if (totalParamCount != params.length) {
            throw new DataAccessException("SQL의 파라미터 갯수가 맞지 않습니다.");
        }
        for (int i = 0; i < totalParamCount; i++) {
            if (!parameterMetaData.getParameterClassName(i + 1).equals(params[i].getClass().getCanonicalName())) {
                throw new DataAccessException("SQL의 파라미터 클래스 타입이 맞지 않습니다.");
            }
        }
        for (int i = 0; i < totalParamCount; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement;
    }
}

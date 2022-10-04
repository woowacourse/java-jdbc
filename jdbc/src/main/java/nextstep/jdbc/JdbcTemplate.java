package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void validateParams(PreparedStatement preparedStatement, List<Object> params) throws SQLException {
        ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
        int totalParamCount = parameterMetaData.getParameterCount();
        if (totalParamCount != params.size()) {
            throw new DataAccessException("SQL의 파라미터 갯수가 맞지 않습니다.");
        }
        for (int i = 0; i < totalParamCount; i++) {
            if (!parameterMetaData.getParameterClassName(i + 1).equals(params.get(i).getClass().getCanonicalName())) {
                throw new DataAccessException("SQL의 파라미터 클래스 타입이 맞지 않습니다.");
            }
        }
        for (int i = 0; i < totalParamCount; i++) {
            preparedStatement.setObject(i + 1, params.get(i));
        }
    }

    public List<List<Object>> executeQuery(String sql, List<Object> params) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
             PreparedStatement pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);
            validateParams(pstmt, params);

            if (sql.toLowerCase().startsWith("select")) {
                List<List<Object>> res = new ArrayList<>();
                ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    res.add(new ArrayList<>());
                    int columnSize = resultSet.getMetaData().getColumnCount();
                    for (int i = 0; i < columnSize; i++) {
                        res.get(res.size() - 1).add(resultSet.getObject(i + 1));
                    }
                }
                return res;
            }
            pstmt.executeUpdate();
            return null;
        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

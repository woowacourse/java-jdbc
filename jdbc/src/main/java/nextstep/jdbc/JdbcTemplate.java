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

    private void validateParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
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
    }

    public List<List<Object>> executeQuery(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection()) {
             PreparedStatement pstmt = conn.prepareStatement(sql);
            log.debug("query : {}", sql);
            validateParams(pstmt, params);

            return generateResultList(sql, pstmt);
        } catch (SQLException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<List<Object>> generateResultList(String sql, PreparedStatement pstmt) throws SQLException {
        if (sql.toLowerCase().startsWith("select")) {
            List<List<Object>> res = new ArrayList<>();
            ResultSet resultSet = pstmt.executeQuery();
            addObjectToResultList(res, resultSet);
            return res;
        }
        pstmt.executeUpdate();
        return null;
    }

    private void addObjectToResultList(List<List<Object>> res, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            res.add(new ArrayList<>());
            int columnSize = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < columnSize; i++) {
                res.get(res.size() - 1).add(resultSet.getObject(i + 1));
            }
        }
    }
}

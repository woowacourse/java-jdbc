package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object obj : args) {
                pstmt.setObject(index++, obj);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object obj : args) {
                pstmt.setObject(index++, obj);
            }
            ResultSet resultSet = pstmt.executeQuery();

            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object obj : args) {
                pstmt.setObject(index++, obj);
            }
            ResultSet resultSet = pstmt.executeQuery();

            List<T> result = new ArrayList<>();
            resultSet.getFetchSize();
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return getSingleResult(result);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T getSingleResult(List<T> result) {
        if (result.size() == 0) {
            throw new EmptyResultDataAccessException("쿼리 실행 결과가 존재하지 않습니다.", 1);
        }
        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("쿼리 실행 결과의 개수가 초과되었습니다.", 1);
        }
        return result.get(0);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

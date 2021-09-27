package nextstep.jdbc;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... objects) {
        assert !Strings.isNullOrEmpty(sql) : "Query is Null And Empty!";
        log.info("update query: {}", sql);
        execute(sql, (PreparedStatement pstmt) -> {
            setValues(pstmt, objects);
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            //todo; Custom Exception 하나 만들어서 통일하기!
            throw new IllegalArgumentException("");
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        List<T> results = query(sql, rowMapper, objects);
        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... objects) {
        assert !Strings.isNullOrEmpty(sql) : "Query is Null And Empty!";
        ResultSetExtractor<List<T>> resultSetExtractor = rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        };

        return query(sql, resultSetExtractor, objects);
    }

    private <T> List<T> query(String sql, ResultSetExtractor<List<T>> resultSetExtractor, Object... objects) {
        return execute(sql, (PreparedStatement pstmt) -> {
            setValues(pstmt, objects);
            try (ResultSet rs = pstmt.executeQuery()) {
                return resultSetExtractor.extractData(rs);
            }
        });
    }

    private void setValues(PreparedStatement pstmt, Object[] objects) {
        IntStream.range(0, objects.length)
                .forEach(it -> setValue(it, pstmt, objects[it]));
    }

    private void setValue(int sequence, PreparedStatement pstmt, Object object) {
        try {
            pstmt.setObject(sequence + 1, object);
        } catch (SQLException e) {
            throw new IllegalArgumentException("SQL 쿼리문의 파라미터에 값을 할당할 수 없습니다.");
        }
    }
}

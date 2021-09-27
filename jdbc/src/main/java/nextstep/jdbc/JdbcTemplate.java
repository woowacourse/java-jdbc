package nextstep.jdbc;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
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
        execute(sql, pstmt -> {
            setValues(pstmt, objects);
            return pstmt.executeUpdate();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        assert !Strings.isNullOrEmpty(sql) : "Query is Null And Empty!";
        execute(sql, pstmt -> {
//            PreparedStatement pstmt = con.prepareStatement(sql);
//            setValues(objects, pstmt);
            return pstmt;
        });
        return Collections.emptyList();
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return callback.execute(pstmt);
        } catch (SQLException e) {
            //todo; Custom Exception 하나 만들어서 통일하기!
            throw new IllegalArgumentException("");
        }
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

//    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
//        assert Strings.isNullOrEmpty(sql) : "Query is Null And Empty!";
//        try (Connection conn = dataSource.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            action.doInConnection(conn);
//        } catch (SQLException e) {
//
//        }
//    }
}

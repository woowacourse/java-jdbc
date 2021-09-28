package nextstep.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.exception.DataAccessException;
import nextstep.exception.ObjectCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        update(sql, createPreparedStatementSetter(args));
    }

    public void update(String sql, PreparedStatementSetter pss) {
        execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> List<T> queryForList(String sql, Class<T> type, Object... args) {
        return query(sql, (rs, rowNum) -> getSingleObject(rs, type), createPreparedStatementSetter(args));
    }

    public <T> Optional<T> queryForObject(String sql, Class<T> type, Object... args) {
        return queryForObject(sql, (rs, rowNum) -> getSingleObject(rs, type), args);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> result = query(sql, new RowMapperExtractor<>(rowMapper), createPreparedStatementSetter(args));

        if (result.size() > 1) {
            throw new DataAccessException("조회된 데이터가 2개 이상입니다.");
        }

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        return query(sql, new RowMapperExtractor<>(rowMapper), pss);
    }

    public <T> List<T> query(String sql, RowMapperExtractor<T> extractor, PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractor.extractData(rs);
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementSetter pss, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pss.setValue(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T getSingleObject(ResultSet rs, Class<T> type) {
        try {
            T object = type.getConstructor().newInstance();
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                field.set(object, rs.getObject(field.getName()));
            }
            return object;
        } catch (Exception e) {
            throw new ObjectCreationException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}


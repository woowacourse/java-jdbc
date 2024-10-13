package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.extractor.ExtractionRule;
import com.interface21.jdbc.core.extractor.ManualExtractor;
import com.interface21.jdbc.core.mapper.ObjectMappedStatement;
import com.interface21.jdbc.core.extractor.ResultSetExtractor;
import com.interface21.jdbc.core.extractor.ReflectiveExtractor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@ParametersAreNonnullByDefault
public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String MULTIPLE_DATA_ERROR = "두 개 이상의 데이터가 조회되었습니다.";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Nullable
    public <T> T queryOne(Class<T> clazz, String sql, Object... params) {
        List<T> result = query(clazz, sql, params);
        validateResultLessOrEqualThanOne(result);

        return result.isEmpty() ? null : result.getFirst();
    }

    private <T> void validateResultLessOrEqualThanOne(List<T> result) {
        if (result.size() > 1) {
            throw new DataAccessException(MULTIPLE_DATA_ERROR);
        }
    }

    public int update(String sql, Object... params) {
        try {
            log.info("update sql = {}", sql);
            PreparedStatement preparedStatement = getPreparedStatement(sql);
            ObjectMappedStatement objectMapper = new ObjectMappedStatement(preparedStatement, params);
            return objectMapper.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public <T> List<T> query(Class<T> clazz, String sql, Object... params) {
        return doQuery(new ReflectiveExtractor<>(getResultSet(sql, params), clazz));
    }

    @Nonnull
    public <T> List<T> query(ExtractionRule<T> extractionRule, String sql, Object... params) {
        return doQuery(new ManualExtractor<>(getResultSet(sql, params), extractionRule));
    }

    private <T> List<T> doQuery(ResultSetExtractor<T> resultSetExtractor) {
        try (resultSetExtractor) {
            return resultSetExtractor.extract();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet getResultSet(String sql, Object[] params) {
        try (ObjectMappedStatement statement = new ObjectMappedStatement(getPreparedStatement(sql), params)) {
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql) throws SQLException {
        return dataSource.getConnection().prepareStatement(sql);
    }
}

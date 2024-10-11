package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.mapper.ObjectMapper;
import com.interface21.jdbc.core.mapper.PreparedStatementMapper;
import com.interface21.jdbc.core.extractor.ResultSetExtractor;
import com.interface21.jdbc.core.extractor.ReflectiveExtractor;
import java.sql.Connection;
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
    public static final String MULTIPLE_DATA_ERROR = "두 개 이상의 데이터가 조회되었습니다.";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Nullable
    public <T> T query(Class<T> clazz, String sqlStatement, Object... params) {
        List<T> result = queryForAll(clazz, sqlStatement, params);
        if (result.isEmpty()) {
            return null;
        }
        validateSizeIsOne(result);

        return result.getFirst();
    }

    private <T> void validateSizeIsOne(List<T> result) {
        if (result.size() > 1) {
            throw new DataAccessException(MULTIPLE_DATA_ERROR);
        }
    }

    public int update(String sqlStatement, Object... params) {
        log.info("update sql = {}", sqlStatement);
        return getMapper(sqlStatement, params).executeUpdate();
    }

    @Nonnull
    public <T> List<T> queryForAll(Class<T> clazz, String sqlStatement, Object... params) {
        log.info("query sql = {}", sqlStatement);
        try (PreparedStatementMapper wrapper = getMapper(sqlStatement, params);
             ResultSet resultSet = wrapper.executeQuery();
             ResultSetExtractor<T> extractor = new ReflectiveExtractor<>(resultSet, clazz)) {

            return extractor.extract();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatementMapper getMapper(String sqlStatement, Object[] params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlStatement)) {

            return new ObjectMapper(ps, params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

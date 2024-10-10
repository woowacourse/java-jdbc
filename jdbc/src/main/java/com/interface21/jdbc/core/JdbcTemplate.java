package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.mapper.SqlResultSetMapper;
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
        if (result.size() > 1) {
            throw new DataAccessException("두 개 이상의 데이터가 조회되었습니다.");
        }

        return result.getFirst();
    }

    @Nonnull
    public <T> List<T> queryForAll(Class<T> clazz, String sqlStatement, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = setStatement(conn, sqlStatement, params);
                ResultSet resultSet = preparedStatement.executeQuery();
        ) {
            log.debug("query : {}", sqlStatement);

            return SqlResultSetMapper.doQueryMapping(clazz, resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void update(String sqlStatement, Object... params) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = setStatement(connection, sqlStatement, params);
        ) {
            log.debug("query : {}", sqlStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    @Nonnull
    private PreparedStatement setStatement(Connection connection, String sqlStatement, Object[] params)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);

        for (int index = 0; index < params.length; index++) {
            int databaseIndex = index + 1;
            preparedStatement.setObject(databaseIndex, params[index]);
        }

        return preparedStatement;
    }
}

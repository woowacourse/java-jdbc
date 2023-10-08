package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int SINGLE_SIZE = 1;
    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        return query(connection, sql, rowMapper, createPreparedStatementSetter(args));
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        return queryForObject(connection, sql, rowMapper, createPreparedStatementSetter(args));
    }

    public void update(Connection connection, String sql, Object... args) {
        update(connection, sql, createPreparedStatementSetter(args));
    }

    public void update(String sql, Object... args) {
        update(sql, createPreparedStatementSetter(args));
    }

    private <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            return mapResultSetToObject(rowMapper, preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> result = query(connection, sql, rowMapper, pss);
        validateResultSize(result);
        return result.get(0);
    }

    private void update(Connection connection, String sql, PreparedStatementSetter pss) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void update(String sql, PreparedStatementSetter pss) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return psmt -> {
            for (int i = 0; i < args.length; i++) {
                psmt.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> List<T> mapResultSetToObject(RowMapper<T> rowMapper, PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.run(resultSet));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static <T> void validateResultSize(List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("해당하는 유저가 없습니다.");
        }

        if(result.size() > SINGLE_SIZE) {
            throw new DataAccessException("해당하는 유저가 2명 이상입니다.");
        }
    }
}

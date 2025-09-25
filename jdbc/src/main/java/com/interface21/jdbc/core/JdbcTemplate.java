package com.interface21.jdbc.core;

import com.interface21.dao.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql,Object... objects) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for(int i=1;i<=objects.length;i++){
                preparedStatement.setObject(i,objects[i-1]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeConnections(preparedStatement, connection);
        }
    }

    public void insert(String sql,Object... objects) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for(int i=1;i<=objects.length;i++){
                preparedStatement.setObject(i,objects[i-1]);
            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeConnections(preparedStatement, connection);
        }
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        List<T> result = new ArrayList<>();

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()){
                return List.of();
            }

            T row = rowMapper.mapRow(resultSet, resultSet.getRow());
            result.add(row);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeConnections(preparedStatement, connection);
        }

        return result;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper,Object... objects) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for(int i=1;i<=objects.length;i++){
                preparedStatement.setObject(i,objects[i-1]);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return rowMapper.mapRow(resultSet, resultSet.getRow());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeConnections(preparedStatement, connection);
        }

        throw new RuntimeException("유효한 데이터를 찾는데 실패하였습니다.");
    }

    private void closeConnections(PreparedStatement preparedStatement, Connection connection) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException ignored) {}

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }
}

package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    protected final DataSource dataSource;

    protected SelectJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object query() {
        String query = createQuery();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            setValues(pstmt);
            ResultSet resultSet = executeQuery(pstmt);

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }
            return mapRow(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public List<Object> queryList() {
        String query = createQuery();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            setValues(pstmt);
            ResultSet resultSet = executeQuery(pstmt);
            List<Object> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeQuery();
    }

    protected abstract void setValues(PreparedStatement preparedStatement) throws SQLException;

    protected abstract String createQuery();

    protected abstract Object mapRow(ResultSet resultSet) throws SQLException;

    protected abstract DataSource getDataSource();
}

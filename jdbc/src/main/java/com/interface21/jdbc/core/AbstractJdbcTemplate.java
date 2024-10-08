package com.interface21.jdbc.core;

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

public abstract class AbstractJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    protected final DataSource dataSource;

    protected AbstractJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(PrepareStatementSetter statementSetter, Object... objects) {
        String query = createQuery();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            log.debug("query : {}", query);
            statementSetter.setValues(pstmt, objects);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> T query(RowMapper<T> mapper, PrepareStatementSetter statementSetter, Object... objects) {
        String query = createQuery();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            statementSetter.setValues(pstmt, objects);
            ResultSet resultSet = executeQuery(pstmt);

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }
            return mapper.map(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> List<T> queryList(RowMapper<T> mapper, PrepareStatementSetter statementSetter, Object... objects) {
        String query = createQuery();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            statementSetter.setValues(pstmt, objects);
            ResultSet resultSet = executeQuery(pstmt);
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeQuery();
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();
}

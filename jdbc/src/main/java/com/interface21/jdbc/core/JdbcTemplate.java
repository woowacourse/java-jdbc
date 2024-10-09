package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final PrepareStatementSetter STATEMENT_SETTER = (preparedStatement, objects) -> {
        if (objects == null) {
            return;
        }
        for (int index = 0; index < objects.length; index++) {
            preparedStatement.setObject(index + 1, objects[index]);
        }
    };

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... objects) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(pstmt, objects);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    /**
     * 생성된 키를 즉시 사용해야 할 경우 이 메서드를 사용합니다.
     *
     * @param query     실행할 SQL 쿼리
     * @param keyHolder 자동 생성된 키를 저장할 keyHolder
     * @param objects   쿼리에 사용할 파라미터 값
     */
    public void update(String query, GeneratedKeyHolder keyHolder, Object... objects) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(pstmt, objects);
            int resultCount = pstmt.executeUpdate();

            if (resultCount != 1) {
                throw new DataAccessException("데이터 삽입 과정에서 문제가 발생하였습니다.");
            }
            addKeyHolder(keyHolder, pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private void addKeyHolder(GeneratedKeyHolder keyHolder, PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                keyHolder.addKey(id);
            }
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> mapper, Object... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(pstmt, objects);
            ResultSet resultSet = pstmt.executeQuery();

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }
            return mapper.map(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    public <T> List<T> queryForList(String query, RowMapper<T> mapper, Object... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(pstmt, objects);
            ResultSet resultSet = pstmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.map(resultSet));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }
}

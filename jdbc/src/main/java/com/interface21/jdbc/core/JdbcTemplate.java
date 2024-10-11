package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataNotFoundException;
import com.interface21.dao.DataSizeNotMatchedException;
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

    /**
     * 쓰기 쿼리를 사용하는 경우 메서드를 사용합니다.
     *
     * @param query   실행할 SQL 쿼리
     * @param objects 쿼리에 사용할 파라미터 값
     */
    public void update(String query, Object... objects) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(preparedStatement, objects);
            preparedStatement.executeUpdate();
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
             PreparedStatement preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(preparedStatement, objects);
            int resultCount = preparedStatement.executeUpdate();

            if (resultCount != 1) {
                throw new DataAccessException("데이터 삽입 과정에서 문제가 발생하였습니다.");
            }
            addKeyHolder(keyHolder, preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private void addKeyHolder(GeneratedKeyHolder keyHolder, PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                keyHolder.addKey(id);
            }
        }
    }

    /**
     * 단 건의 읽기 쿼리를 사용하는 경우 이 메서드를 사용합니다.
     *
     * @param query   실행할 SQL 쿼리
     * @param mapper  객체로 매핑할 mapper
     * @param objects 쿼리에 사용할 파라미터 값
     * @return mapper로 매핑이 완료된 객체
     * @throws DataNotFoundException 데이터가 존재하지 않는 경우 예외가 발생
     */
    public <T> T queryForObject(String query, RowMapper<T> mapper, Object... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(preparedStatement, objects);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new DataNotFoundException("데이터가 존재하지 않습니다.");
            }

            T result = mapper.map(resultSet);

            if (resultSet.next()) {
                throw new DataSizeNotMatchedException("의도한 데이터와 쿼리 결과의 개수가 일치하지 않습니다.");

            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    /**
     * 다수 건의 읽기 쿼리를 사용하는 경우 이 메서드를 사용합니다.
     *
     * @param query   실행할 SQL 쿼리
     * @param mapper  객체로 매핑할 mapper
     * @param objects 쿼리에 사용할 파라미터 값
     * @return mapper로 매핑이 완료된 객체
     */
    public <T> List<T> queryForList(String query, RowMapper<T> mapper, Object... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            log.debug("query : {}", query);
            STATEMENT_SETTER.setValues(preparedStatement, objects);
            ResultSet resultSet = preparedStatement.executeQuery();
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

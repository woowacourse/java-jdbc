package com.interface21.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;

public final class ResultMapper {

    private static final Logger log = LoggerFactory.getLogger(ResultMapper.class);

    private ResultMapper() {
    }

    public static <T> List<T> multipleResultMapping(RowMapper<T> rowMapper, ResultSet resultSet) {
        try {
            return getMultipleResult(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error("다중 결과 조회 중 SQL 예외가 발생했습니다. 예외 메시지: {}", e.getMessage(), e);
            throw new DataAccessException("다중 결과 조회 중 오류가 발생했습니다. 원인: " + e.getMessage(), e);
        }
    }

    private static <T> List<T> getMultipleResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    public static <T> T singleResultMapping(RowMapper<T> rowMapper, ResultSet resultSet) {
        try {
            return getSingResult(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error("단일 결과 조회 중 SQL 예외가 발생했습니다. 예외 메시지: {}", e.getMessage(), e);
            throw new DataAccessException("단일 결과 조회 중 오류가 발생했습니다. 원인: " + e.getMessage(), e);
        }
    }

    private static <T> T getSingResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);
            validateSingleResult(resultSet);
            return result;
        }
        log.error("단일 행 조회를 기대했으나, 조회된 데이터가 없습니다.");
        throw new DataAccessException("단일 행 조회를 기대했지만, 조회된 행이 없습니다.");
    }

    private static void validateSingleResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            log.error("단일 행 조회를 기대했으나, 여러 행이 조회되었습니다.");
            throw new DataAccessException("단일 행 조회를 기대했지만, 여러 행이 조회되었습니다.");
        }
    }
}

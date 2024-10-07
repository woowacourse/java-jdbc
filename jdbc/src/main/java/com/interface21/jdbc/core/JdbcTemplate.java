package com.interface21.jdbc.core;

import com.interface21.jdbc.exception.ConnectionFailException;
import com.interface21.jdbc.exception.QueryExecutionException;
import com.interface21.jdbc.exception.QueryParseException;
import com.interface21.jdbc.result.MultiSelectResult;
import com.interface21.jdbc.result.SingleSelectResult;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.interface21.jdbc.core.StatementSetter.setStatementsWithPOJOType;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void command(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            try {
                pstmt.executeUpdate();
            } catch (final SQLException exception) {
                throw new QueryExecutionException("쿼리 실행중 예외가 발생했습니다", exception);
            }
        } catch (final SQLException exception) {
            throw new ConnectionFailException("연결을 실패했습니다", exception);
        }
    }

    public SingleSelectResult querySingle(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return parseSingleSelect(rs);
            } catch (final SQLException exception) {
                throw new QueryExecutionException("쿼리 실행중 예외가 발생했습니다", exception);
            }
        } catch (final SQLException exception) {
            throw new ConnectionFailException("연결을 실패했습니다", exception);
        }
    }

    public MultiSelectResult queryMulti(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return parseMultiSelect(rs);
            } catch (final SQLException exception) {
                throw new QueryExecutionException("쿼리 실행중 예외가 발생했습니다", exception);
            }
        } catch (final SQLException exception) {
            throw new ConnectionFailException("연결을 실패했습니다", exception);
        }
    }


    private SingleSelectResult parseSingleSelect(final ResultSet resultSet) {
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final Map<String, Object> map = new HashMap<>();
            if (resultSet.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
            }
            return new SingleSelectResult(map);
        } catch (final SQLException e) {
            throw new QueryParseException("단일 데이터 변환 중 에러가 발생했습니다.", e);
        }

    }

    private MultiSelectResult parseMultiSelect(final ResultSet resultSet) {
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            final List<SingleSelectResult> results = new ArrayList<>();
            while (resultSet.next()) {
                final Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                results.add(new SingleSelectResult(map));
            }
            return new MultiSelectResult(results);
        } catch (final SQLException e) {
            throw new QueryParseException("멀티 데이터 변환 중 에러가 발생했습니다.", e);
        }
    }
}

package com.interface21.jdbc.core;

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
            pstmt.executeUpdate();
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public SingleSelectResult querySingle(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return parseSingleSelect(rs);
            }
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public MultiSelectResult queryMulti(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            try (final ResultSet rs = pstmt.executeQuery()) {
                return parseMultiSelect(rs);
            }
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }


    private SingleSelectResult parseSingleSelect(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final Map<String, Object> map = new HashMap<>();
        if (resultSet.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
        }
        return new SingleSelectResult(map);
    }

    private MultiSelectResult parseMultiSelect(final ResultSet resultSet) throws SQLException {
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
    }
}

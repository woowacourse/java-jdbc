package com.interface21.jdbc.core;

import com.interface21.jdbc.result.SelectMultiResult;
import com.interface21.jdbc.result.SelectSingleResult;

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

    public void write(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            pstmt.executeUpdate();
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public SelectSingleResult selectOne(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return parseSelectSingle(rs);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public SelectMultiResult selectMulti(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return parseSelectMulti(rs);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }


    private SelectSingleResult parseSelectSingle(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final Map<String, Object> map = new HashMap<>();
        if (resultSet.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
        }
        return new SelectSingleResult(map);
    }

    private SelectMultiResult parseSelectMulti(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();

        final List<SelectSingleResult> results = new ArrayList<>();
        while (resultSet.next()) {
            final Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            results.add(new SelectSingleResult(map));
        }
        return new SelectMultiResult(results);
    }
}

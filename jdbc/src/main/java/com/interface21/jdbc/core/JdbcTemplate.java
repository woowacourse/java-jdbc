package com.interface21.jdbc.core;

import com.interface21.jdbc.JdbcTypeMapper;
import com.interface21.jdbc.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameter(params, pstmt);

            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Optional<Object> queryForObject(
            final String sql,
            final ResultSetMapper<?> mapper,
            final Object... params
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameter(params, pstmt);

            try (final ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapper.map(resultSet));
                }
            }

            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<Object> queryForList(
            final String sql,
            final ResultSetMapper<?> mapper,
            final Object... params
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameter(params, pstmt);

            try (final ResultSet resultSet = pstmt.executeQuery()) {
                List<Object> results = new ArrayList<>();

                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }
                return results;
            }
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameter(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        if (params.length == 0) {
            return;
        }

        for (int idx = 1; idx <= params.length; idx++) {
            Object param = params[idx - 1];

            JdbcTypeMapper mapper = JdbcTypeMapper.fromClassType(param);
            mapper.map(pstmt, idx, param);
        }
    }
}

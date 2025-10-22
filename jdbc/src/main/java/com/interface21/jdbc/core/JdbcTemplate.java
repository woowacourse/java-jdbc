package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.bind.RowMapper;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <R> R execute(Function<Connection, R> executor) {
        boolean hasConnection = DataSourceUtils.hasConnectionInThread(dataSource);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return executor.apply(connection);
        } finally {
            if (!hasConnection) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    public int update(String sql, Object... params) {
        return execute(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                log.debug("query : {}", sql);

                setParameters(pstmt, params);
                return pstmt.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        });
    }

    public <T> List<T> find(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                setParameters(pstmt, params);

                log.debug("query : {}", sql);

                List<T> result = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(rowMapper.map(rs));
                    }
                }

                return result;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        });
    }

    public <T> T findOne(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                setParameters(pstmt, params);

                log.debug("query : {}", sql);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rowMapper.map(rs);
                    }
                }

                return null;
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new DataAccessException(e);
            }
        });
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
        if (parameterMetaData.getParameterCount() != params.length) {
            throw new SQLDataException("파라미터 개수가 쿼리 매핑 데이터 개수와 다릅니다.");
        }

        for (int idx = 0; idx < params.length; idx++) {
            pstmt.setObject(idx + 1, params[idx]);
        }
    }
}

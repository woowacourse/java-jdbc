package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementParamMapping preparedStatementParamMapping;
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementParamMapping = new PreparedStatementParamMapping();
        this.dataSource = dataSource;
    }

    public void handleQuery(final String sql, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                setStatementParameter(pstmt, i + 1, parameters[i]);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object handleQueryAndGet(final String sql, final Function<ResultSet, Object> resultSetMapper,
                                    final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                setStatementParameter(pstmt, i + 1, parameters[i]);
            }

            ResultSet rs = pstmt.executeQuery();

            return resultSetMapper.apply(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setStatementParameter(final PreparedStatement pstmt, final int index, final Object value) throws SQLException {
        String typeName = value.getClass().getTypeName();
        preparedStatementParamMapping.callSetter(typeName, pstmt, index, value);
    }
}

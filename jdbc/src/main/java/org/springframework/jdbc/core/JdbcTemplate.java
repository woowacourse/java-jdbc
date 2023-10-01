package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadGrammarJdbcException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.JdbcException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final SqlExceptionTranslator sqlExceptionTranslator = new SqlExceptionTranslator();

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments)
        ) {
            log.debug("query : {}", sql);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw sqlExceptionTranslator.translateException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments);
                ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs));
            }

            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw sqlExceptionTranslator.translateException(e);
        }
    }


    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = StatementCreator.createStatement(conn, sql, arguments);
                ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }

            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw sqlExceptionTranslator.translateException(e);
        }
    }

    private class SqlExceptionTranslator {
        private final Set<String> BAD_SQL_GRAMMAR_CODES = Set.of(
                "07",  // Dynamic SQL error
                "21",  // Cardinality violation
                "2A",  // Syntax error direct SQL
                "37",  // Syntax error dynamic SQL
                "42",  // General SQL syntax error
                "65"   // Oracle: unknown identifier
        );

        private final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = Set.of(
                "08",  // Connection exception
                "53",  // PostgreSQL: insufficient resources (e.g. disk full)
                "54",  // PostgreSQL: program limit exceeded (e.g. statement too complex)
                "57",  // DB2: out-of-memory exception / database not started
                "58"   // DB2: unexpected system error
        );

        private JdbcException translateException(SQLException e) {
            String sqlState = e.getSQLState();
            String classCode = sqlState.substring(0, 2);
            if (DATA_ACCESS_RESOURCE_FAILURE_CODES.contains(classCode)) {
                throw new CannotGetJdbcConnectionException(e.getMessage(), e);
            }
            if (BAD_SQL_GRAMMAR_CODES.contains(classCode)) {
                throw new BadGrammarJdbcException(e.getMessage(), e);
            }

            throw new JdbcException(e.getMessage(), e);
        }
    }
}

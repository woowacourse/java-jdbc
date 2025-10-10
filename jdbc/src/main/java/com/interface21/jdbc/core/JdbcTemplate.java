package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) throws DataAccessException {
        this.dataSource = dataSource;
        testConnection(dataSource);
    }

    public void update(String sql, Object... args) {
        update(sql, PreparedStatementSetter.ofSequenced(args));
    }

    public void update(String sql, PreparedStatementSetter pss) {
        execute(PreparedStatement::execute, sql, pss);
    }

    public <T> T selectOne(RowMapper<T> rowMapper, String sql, Object... args) {
        return selectOne(rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> T selectOne(RowMapper<T> rowMapper, String sql, PreparedStatementSetter pss) {
        StatementExecutor<T> stmtExecutor = pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        };

        return execute(stmtExecutor, sql, pss);
    }

    public <T> List<T> selectMulti(RowMapper<T> rowMapper, String sql, Object... args) {
        return selectMulti(rowMapper, sql, PreparedStatementSetter.ofSequenced(args));
    }

    public <T> List<T> selectMulti(RowMapper<T> rowMapper, String sql, PreparedStatementSetter pss) {
        StatementExecutor<List<T>> stmtExecutor = pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            var list = new ArrayList<T>();
            while (rs.next()) {
                var mappedRow = rowMapper.mapRow(rs);
                list.add(mappedRow);
            }
            return list;
        };

        return execute(stmtExecutor, sql, pss);
    }

    private <T> T execute(StatementExecutor<T> stmtExecutor, String sql, PreparedStatementSetter pss) {
        ResultSet rs = null;
        try (var conn = dataSource.getConnection();
             var pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            pss.setValues(pstmt);
            return stmtExecutor.execute(pstmt);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);

        } finally {
            closeResultSet(rs);
        }
    }

    private void testConnection(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            var databaseProductName = connection.getMetaData().getDatabaseProductName();
            log.info("Connection established to database : {}", databaseProductName);

        } catch (NullPointerException e) {
            log.error("Connection is null on dataSource {}", dataSource);
            throw new DataAccessException(e);

        } catch (SQLException e) {
            log.error(e.getMessage(), e.getCause());
            throw new DataAccessException(e);
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs == null) {
            return;
        }

        try {
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

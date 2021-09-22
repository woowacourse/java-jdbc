package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String query, Object... args) {
        updateWithStatementStrategy(new StatementStrategy() {
            @Override
            public PreparedStatement makePreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(query);
                setParameters(ps, args);
                return ps;
            }
        });
    }

    private void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private void updateWithStatementStrategy(StatementStrategy stmt) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = dataSource.getConnection();

            ps = stmt.makePreparedStatement(conn);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if(ps != null){
                try{
                    ps.close();
                } catch (SQLException e){}
            }
            if(conn != null){
                try{
                    conn.close();
                } catch (SQLException e){}
            }
        }
    }

    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) {
        return query(
            (conn) -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                setParameters(preparedStatement, args);
                return preparedStatement;
            },
            rse
        );
    }

    public <T> T query(StatementStrategy stmt, ResultSetExtractor<T> rse) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = dataSource.getConnection();

            ps = stmt.makePreparedStatement(conn);

            ResultSet rs = ps.executeQuery();
            return rse.extractData(rs);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if(ps != null){
                try{
                    ps.close();
                } catch (SQLException e){}
            }
            if(conn != null){
                try{
                    conn.close();
                } catch (SQLException e){}
            }
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(
            (conn) -> conn.prepareStatement(sql),
            rowMapper
        );
    }

    public <T> List<T> query(StatementStrategy stmt, RowMapper<T> rowMapper) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = dataSource.getConnection();

            ps = stmt.makePreparedStatement(conn);

            ResultSet rs = ps.executeQuery();

            List<T> result = new ArrayList<>();

            int rowNum = 1;
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs, rowNum));
                rowNum++;
            }

            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if(ps != null){
                try{
                    ps.close();
                } catch (SQLException e){}
            }
            if(conn != null){
                try{
                    conn.close();
                } catch (SQLException e){}
            }
        }
    }
}

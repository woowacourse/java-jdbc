package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultSetDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSetSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JdbcTemplate {
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, conn, args);
             ResultSet rs = pstmt.executeQuery()) {
            return getSingleResult(rowMapper, rs);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, conn, args);
             ResultSet rs = pstmt.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = generatePreparedStatement(sql, conn, args)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement generatePreparedStatement(
            String sql,
            Connection connection,
            Object... args) throws SQLException {
        Objects.requireNonNull(args);
        PreparedStatement pstmt = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }

    private <T> T getSingleResult(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new EmptyResultSetDataAccessException();
        }
        T target = rowMapper.map(resultSet);
        if (resultSet.next()) {
            throw new IncorrectResultSetSizeDataAccessException();
        }
        return target;
    }
}

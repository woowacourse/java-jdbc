package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = generatePreparedStatement(sql, conn, args)) {
            return pstmt.executeUpdate();
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage(), sqlException);
            throw new DataAccessException(sqlException);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs, rs.getRow()));
            }
            return list;
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage(), sqlException);
            throw new DataAccessException(sqlException);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = generatePreparedStatement(sql, conn, args);
            ResultSet rs = pstmt.executeQuery()) {
            return requiredSingleResult(rowMapper, rs);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage(), sqlException);
            throw new DataAccessException(sqlException);
        }
    }

    private PreparedStatement generatePreparedStatement(
        String sql,
        Connection connection,
        Object... args
    ) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setString(i + 1, (String) args[i]);
        }
        return preparedStatement;
    }

    private <T> T requiredSingleResult(
        RowMapper<T> rowMapper,
        ResultSet resultSet
    ) throws SQLException {
        if (!resultSet.next()) {
            throw new EmptyResultDataException();
        }
        T target = rowMapper.mapRow(resultSet, resultSet.getRow());
        if (resultSet.next()) {
            throw new IncorrectResultSizeDataAccessException();
        }
        return target;
    }
}

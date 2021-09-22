package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static DataSource dataSource;

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        try(connection; statement) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i, args[0]);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                return rowMapper.mapRow(rs, rs.getRow());
            }
        }

        throw new SQLException("error");
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        List<T> results = new ArrayList<>();

        try(connection; statement) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i, args[0]);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rs.getRow()));
            }

            return results;
        }
    }

}

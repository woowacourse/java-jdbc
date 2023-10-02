package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate extends AutoClosableTemplate {

    public JdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void commandQuery(final PreparedStatement pstmt) throws SQLException {
        pstmt.executeUpdate();
    }

    @Override
    protected <T> List<T> queryAll(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();

        while (rs.next()) {
            T object = rowMapper.mapRow(rs);
            results.add(object);
        }

        return results;
    }

    @Override
    protected <T> Optional<T> queryForOne(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        while (rs.next()) {
            T object = rowMapper.mapRow(rs);
            return Optional.ofNullable(object);
        }

        return Optional.empty();
    }
}

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;

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
        final List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(rowMapper.mapRow(rs));
        }
        return list;
    }

    @Nullable
    @Override
    protected <T> T queryForOne(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        if (rs.next()) {
            return rowMapper.mapRow(rs);
        }
        return null;
    }
}

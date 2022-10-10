package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import nextstep.jdbc.callback.ExecuteCallback;
import nextstep.jdbc.callback.RowMapperCallback;

public class JdbcUtil {

	private static final Logger log = LoggerFactory.getLogger(JdbcUtil.class);

	private final DataSource dataSource;

	public JdbcUtil(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Object execute(final String sql, final ExecuteCallback executeCallback,
		final RowMapperCallback rowMapperCallback) {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		try (PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = executeCallback.execute(pstmt)) {
			return rowMapperCallback.map(rs);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e);
		}
	}
}

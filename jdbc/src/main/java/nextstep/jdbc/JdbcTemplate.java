package nextstep.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SqlBuilder createQuery(String sql) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			close(conn, pstmt);
			throw new RuntimeException(e);
		}
		return new SqlBuilder(conn, pstmt);
	}

	public static class SqlBuilder {

		private final Connection conn;
		private final PreparedStatement pstmt;
		private ResultSet rs;

		public SqlBuilder(Connection conn, PreparedStatement pstmt) {
			this.conn = conn;
			this.pstmt = pstmt;
		}

		public SqlBuilder setString(int parameterIndex, String parameter) {
			try {
				pstmt.setString(parameterIndex, parameter);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				close(conn, pstmt);
				throw new RuntimeException(e);
			}
			return this;
		}

		public SqlBuilder setLong(int parameterIndex, Long parameter){
			try {
				pstmt.setLong(parameterIndex, parameter);
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				close(conn, pstmt);
				throw new RuntimeException(e);
			}
			return this;
		}

		public void executeUpdate() {
			try {
				pstmt.executeUpdate();
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				close(conn, pstmt);
			}
		}

		public SqlBuilder executeQuery() {
			try {
				this.rs = pstmt.executeQuery();
				return this;
			} catch (SQLException e) {
				log.error(e.getMessage(), e);
				close(conn, pstmt);
				throw new RuntimeException(e);
			}
		}

		public <T> T getResult(Class<T> type) {
			try {
				Field[] fields = type.getDeclaredFields();
				T result = newInstance(type);
				if (rs.next()) {
					createResult(fields, result);
				}
				return result;
			} catch (SQLException | IllegalAccessException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				close(conn, pstmt);
			}
		}

		private <T> T newInstance(Class<T> type) {
			T result;
			try {
				result = type.getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
			return result;
		}

		private <T> void createResult(Field[] fields, T result) throws IllegalAccessException, SQLException {
			int parameterIndex = 1;
			for (Field field : fields) {
				field.setAccessible(true);
				Class<?> fieldType = field.getType();
				if (fieldType.equals(Long.class)) {
					field.set(result, rs.getLong(parameterIndex++));
				}
				if (fieldType.equals(String.class)) {
					field.set(result, rs.getString(parameterIndex++));
				}
			}
		}

	}

	private static void close(Connection conn, PreparedStatement pstmt) {
		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException ignored) {
		}
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException ignored) {
		}
	}
}

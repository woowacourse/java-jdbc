package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class JdbcTemplateTest {

	private static final JdbcDataSource JDBC_DATA_SOURCE = new JdbcDataSource();

	private static final RowMapper<User> ROW_MAPPER = (resultSet) -> {
		long id = resultSet.getLong("id");
		String account = resultSet.getString("account");
		String password = resultSet.getString("password");
		String email = resultSet.getString("email");
		return new User(id, account, password, email);
	};
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() throws SQLException {
		JDBC_DATA_SOURCE.setURL("jdbc:h2:tcp://localhost/~/mem:test");
		JDBC_DATA_SOURCE.setUser("");
		JDBC_DATA_SOURCE.setPassword("");
		jdbcTemplate = new JdbcTemplate(JDBC_DATA_SOURCE);

		String schemaSql = "create table if not exists users (\n"
			+ "    id bigint auto_increment,\n"
			+ "    account varchar(100) not null,\n"
			+ "    password varchar(100) not null,\n"
			+ "    email varchar(100) not null,\n"
			+ "    primary key(id)\n"
			+ ");\n";

		try (Connection connection = JDBC_DATA_SOURCE.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(schemaSql)) {
			preparedStatement.execute();
		}
	}

	@DisplayName("executeQuery 메서드로 데이터 insert")
	@Test
	void insertWithExecuteQuery() {
		String sql = "insert into users(account, password, email) values(?, ?, ?);";
		assertDoesNotThrow(() -> jdbcTemplate.executeQuery(sql, "fafi", "1234", "fafi@gmail.com"));
	}

	@DisplayName("queryForObject로 단건 데이터 조회")
	@Test
	void queryForObject() {
		String account = "fafi";
		String selectSql = "select * from users where account = ?";
		User user = jdbcTemplate.queryForObject(selectSql, ROW_MAPPER, account);

		assertThat(user.getId()).isNotNull();
		assertThat(user.getPassword()).isEqualTo("1234");
		assertThat(user.getEmail()).isEqualTo("fafi@gmail.com");
	}

	@DisplayName("executeQuery 메서드로 데이터 update")
	@Test
	void updateWithExecuteQuery() {
		String updateSql = "update users set password = ? where account = ?";
		jdbcTemplate.executeQuery(updateSql, "123", "fafi");

		String selectSql = "select * from users where account = ?";
		User fafi = jdbcTemplate.queryForObject(selectSql, ROW_MAPPER, "fafi");

		assertThat(fafi.getPassword()).isEqualTo("123");
	}

	@DisplayName("query 메서드로 여러 데이터 조회")
	@Test
	void selectAllWithQuery() {
		String selectSql = "select * from users";
		List<User> users = jdbcTemplate.query(selectSql, ROW_MAPPER);
		assertNotNull(users);
	}

	private static class User {

		private Long id;
		private final String account;
		private String password;
		private final String email;

		public User(long id, String account, String password, String email) {
			this.id = id;
			this.account = account;
			this.password = password;
			this.email = email;
		}

		public User(String account, String password, String email) {
			this.account = account;
			this.password = password;
			this.email = email;
		}

		public Long getId() {
			return id;
		}

		public String getAccount() {
			return account;
		}

		public String getPassword() {
			return password;
		}

		public String getEmail() {
			return email;
		}
	}
}
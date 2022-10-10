package nextstep.jdbc;

import static nextstep.jdbc.Extractor.extractData;
import static nextstep.jdbc.UserFixture.수달;
import static nextstep.jdbc.UserFixture.조시;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class JdbcTemplateTest {

	private static final String FIND_BY_ACCOUNT = "SELECT * FROM users WHERE account = ?";
	private static MockedStatic<Extractor> Extractor;

	private JdbcTemplate jdbcTemplate;
	private DataSource dataSource;
	private Connection connection;
	private PreparedStatement preparedStatement;
	private ResultSet resultSet;

	@BeforeEach
	void setUp() throws SQLException {
		this.dataSource = mock(DataSource.class);
		this.connection = mock(Connection.class);
		this.preparedStatement = mock(PreparedStatement.class);
		this.resultSet = mock(ResultSet.class);

		this.jdbcTemplate = new JdbcTemplate(dataSource);

		given(dataSource.getConnection()).willReturn(connection);
		given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
		given(preparedStatement.executeQuery()).willReturn(resultSet);
	}

	@BeforeAll
	public static void beforeALl() {
		Extractor = mockStatic(Extractor.class);
	}

	@Test
	void 쿼리를_실행시켜_한_개의_엔티티를_반환한다() throws SQLException {
		String findAccount = 수달.getAccount();
		// given
		Extractor.when(() -> extractData(any(), any()))
			.thenReturn(Arrays.asList(수달));

		final User user = jdbcTemplate.queryForObject(FIND_BY_ACCOUNT, User.class, findAccount);

		// then
		assertThat(user).extracting("account").isEqualTo(findAccount);
	}

	@Test
	void 쿼리를_실행시켜_한_개의_데이터를_찾아오는데_결과가_없는_경우_예외가_발생한다() throws SQLException {
		// given
		Extractor.when(() -> extractData(any(), any()))
			.thenReturn(Arrays.asList());

		// when, then
		assertThatThrownBy(() -> jdbcTemplate.queryForObject(FIND_BY_ACCOUNT, User.class, "안깜찍_수달"))
			.isInstanceOf(DataAccessException.class)
			.hasMessage("일치하는 데이터가 없습니다.");
	}

	@Test
	void 쿼리를_실행시켜_한_개의_데이터를_찾아오는데_결과가_두_개_이상일_경우_예외가_발생한다() throws SQLException {
		// given
		Extractor.when(() -> extractData(any(), any()))
			.thenReturn(Arrays.asList(수달, 조시));

		// when, then
		assertThatThrownBy(() -> jdbcTemplate.queryForObject(FIND_BY_ACCOUNT, User.class))
			.isInstanceOf(DataAccessException.class)
			.hasMessage("조회 데이터 갯수가 2 입니다.");
	}

}

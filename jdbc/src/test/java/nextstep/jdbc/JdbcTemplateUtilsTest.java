package nextstep.jdbc;

import static nextstep.jdbc.UserFixture.수달;
import static nextstep.jdbc.UserFixture.조시;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class JdbcTemplateUtilsTest {

	@Test
	void 단일값_Empty_예외_반환() {
		final List<User> users = Arrays.asList();
		assertThatThrownBy(() -> JdbcTemplateUtils.singleResult(users))
			.isInstanceOf(DataAccessException.class);
	}

	@Test
	void 단일값_정상시_반환() {
		final List<User> users = Arrays.asList(조시);
		final User user = JdbcTemplateUtils.singleResult(users);

		assertThat(user).isEqualTo(조시);
	}

	@Test
	void 단일값_두개시_예외_반환() {
		final List<User> users = Arrays.asList(조시, 수달);
		assertThatThrownBy(() -> JdbcTemplateUtils.singleResult(users))
			.isInstanceOf(DataAccessException.class);
	}

}

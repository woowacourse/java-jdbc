package nextstep.jdbc.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class DataAccessUtilsTest {

    private final ResultSet resultSet = mock(ResultSet.class);

    @Test
    void objectResult() throws SQLException {
        //given
        RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
                rs.getString("name"),
                rs.getInt("age")
        );
        given(resultSet.next()).willReturn(true);
        given(resultSet.getString("name")).willReturn("Arthur");
        given(resultSet.getInt("age")).willReturn(20);

        //when
        User user = DataAccessUtils.objectResult(userRowMapper, resultSet);

        //then
        assertAll(
                () -> assertThat(user.name).isEqualTo("Arthur"),
                () -> assertThat(user.age).isEqualTo(20)
        );
    }

    @Test
    void listResult() throws SQLException {
        //given
        RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
                rs.getString("name"),
                rs.getInt("age")
        );
        given(resultSet.next()).willReturn(true).willReturn(true).willReturn(false);
        given(resultSet.getString("name")).willReturn("Arthur").willReturn("ChicChoc");
        given(resultSet.getInt("age")).willReturn(20);

        //when
        List<User> users = DataAccessUtils.listResult(userRowMapper, resultSet);

        //then
        assertAll(
                () -> assertThat(users.size()).isEqualTo(2),
                () -> assertThat(users.get(1).name).isEqualTo("ChicChoc")
        );
    }

    class User {
        private final String name;
        private final int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}

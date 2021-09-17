package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class RowMapperTest {

    @Test
    void rowMap() throws SQLException {
        RowMapper<User> userRowMapper = rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("account")).thenReturn("account");
        when(rs.getString("password")).thenReturn("password");
        when(rs.getString("email")).thenReturn("email@email.com");

        User user = userRowMapper.mapRow(rs);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("account");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("email@email.com");
    }
}

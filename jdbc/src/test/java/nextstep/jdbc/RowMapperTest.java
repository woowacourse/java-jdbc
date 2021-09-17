package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

public class RowMapperTest {

    @Test
    void rowMap() throws SQLException {
        RowMapper<User> userRowMapper = rs -> new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong(1)).thenReturn(1L);
        when(rs.getString(2)).thenReturn("account");
        when(rs.getString(3)).thenReturn("password");
        when(rs.getString(4)).thenReturn("email@email.com");

        User user = userRowMapper.mapRow(rs);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("account");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("email@email.com");
    }
}

package com.interface21.jdbc.core.extractor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.JdbcTemplateTest;
import com.interface21.jdbc.mapper.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ManualExtractorTest extends JdbcTemplateTest {


    @DisplayName("ManualExtractor을 사용해 데이터를 가져올 수 있다.")
    @Test
    void test() throws SQLException {
        User expected = new User("1", "one");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(expected.getId());
        when(resultSet.getString("name")).thenReturn(expected.getName());
        ResultSetExtractor<User> extractor = new ManualExtractor<>(resultSet,
                rs -> new User(rs.getString("id"), rs.getString("name"))
        );

        User actual = extractor.extractOne();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void extractOne() {
    }
}

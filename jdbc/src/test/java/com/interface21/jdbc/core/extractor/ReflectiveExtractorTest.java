package com.interface21.jdbc.core.extractor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReflectiveExtractorTest {

    @DisplayName("ReflectiveExtractor 사용해 데이터를 가져올 수 있다.")
    @Test
    void extractOne() throws SQLException {
        User expected = new User("1", "one");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("id")).thenReturn(expected.getId());
        when(resultSet.getObject("name")).thenReturn(expected.getName());
        ReflectiveExtractor<User> extractor = new ReflectiveExtractor<>(resultSet, User.class);

        User actual = extractor.extractOne();

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}

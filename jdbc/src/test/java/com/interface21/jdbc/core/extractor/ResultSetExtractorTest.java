package com.interface21.jdbc.core.extractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import com.interface21.jdbc.mapper.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultSetExtractorTest {


    @DisplayName("자원을 정상적으로 반환하지 못하면 예외가 발생한다.")
    @Test
    void close() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ManualExtractor<User> extractor = new ManualExtractor<>(resultSet, rs -> new User());
        doThrow(new SQLException()).when(resultSet).close();

        Assertions.assertThatThrownBy(extractor::close)
                .isInstanceOf(CannotReleaseJdbcResourceException.class);
    }

    @DisplayName("리스트가 개수의 개수만큼 반환되는지 확인한다.")
    @Test
    void extract() throws SQLException {
        User user = new User("1", "one");
        User user2 = new User("2", "two");
        User user3 = new User("3", "three");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("id")).thenReturn(user.getId(), user2.getId(), user3.getId());
        when(resultSet.getString("name")).thenReturn(user.getName(), user2.getName(), user3.getName());
        ResultSetExtractor<User> extractor = new ManualExtractor<>(resultSet,
                rs -> new User(rs.getString("id"), rs.getString("name"))
        );

        List<User> users = extractor.extract();

        Assertions.assertThat(users).contains(user, user2, user3);
    }
}

package com.interface21.jdbc.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class SqlResultSetMapperTest {

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        reset(mock(SqlResultSetMapper.class));
    }

    @DisplayName("쿼리 결과가 없다면 빈리스트를 반환한다.")
    @Test
    void queryNone() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.isAfterLast()).thenReturn(true);

        List<String> result = SqlResultSetMapper.doQueryMapping(String.class, resultSet);

        Assertions.assertThat(result).isEmpty();

    }

    @DisplayName("쿼리 결과를 반환한다.")
    @Test
    void queryResult() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.isAfterLast()).thenReturn(false);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject("id")).thenReturn("one", "two");
        when(resultSet.getObject("name")).thenReturn("paper", "whiteTiger");
        User paper = new User("one", "paper");
        User whiteTiger = new User("two", "whiteTiger");

        List<User> result = SqlResultSetMapper.doQueryMapping(User.class, resultSet);

        Assertions.assertThat(result).contains(paper, whiteTiger);
    }

}

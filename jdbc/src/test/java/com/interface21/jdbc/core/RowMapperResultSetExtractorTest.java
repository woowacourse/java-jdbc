package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.core.support.User;
import com.interface21.jdbc.core.support.UserRowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    private RowMapperResultSetExtractor<User> rse;

    @BeforeEach
    void setUp() {
        final var userRowMapper = new UserRowMapper();
        rse = new RowMapperResultSetExtractor<>(userRowMapper);
    }

    @DisplayName("ResultSet에서 데이터를 추출할 수 있다.")
    @Test
    void extractData() throws SQLException {
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("jerry");

        final var actual = rse.extractData(rs);

        assertThat(actual).containsExactly(new User(1L, "jerry"));
    }
}

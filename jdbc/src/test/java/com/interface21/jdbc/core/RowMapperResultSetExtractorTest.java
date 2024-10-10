package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RowMapperResultSetExtractorTest {

    private static final RowMapper<TestUser> ROW_MAPPER = rs ->
            new TestUser(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));

    private ResultSet rs;

    @BeforeEach
    void setUp() {
        rs = mock(ResultSet.class);
    }

    @Test
    void ResultSet의_데이터를_담은_리스트를_반환한다() throws SQLException {
        // given
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("account")).thenReturn("prin", "waterfall");
        when(rs.getString("password")).thenReturn("1q2w3e4r!@", "1q2w3e4r!@");
        when(rs.getString("email")).thenReturn("prin@gmail.com", "waterfall@gmail.com");

        ResultSetExtractor<List<TestUser>> extractor = new RowMapperResultSetExtractor<>(ROW_MAPPER);

        // when
        List<TestUser> testUsers = extractor.extractData(rs);

        // then
        assertSoftly(softly -> {
            softly.assertThat(testUsers).hasSize(2);
            softly.assertThat(testUsers).extracting("id").containsExactly(1L, 2L);
            softly.assertThat(testUsers).extracting("account").containsExactly("prin", "waterfall");
            softly.assertThat(testUsers).extracting("password").containsExactly("1q2w3e4r!@", "1q2w3e4r!@");
            softly.assertThat(testUsers).extracting("email").containsExactly("prin@gmail.com", "waterfall@gmail.com");
        });
    }

    @Test
    void ResultSet이_비어있으면_빈_리스트를_반환한다() throws SQLException {
        // given
        when(rs.next()).thenReturn(false);
        ResultSetExtractor<List<TestUser>> extractor = new RowMapperResultSetExtractor<>(ROW_MAPPER);

        // when
        List<TestUser> testUsers = extractor.extractData(rs);

        // then
        assertThat(testUsers).isEmpty();
    }

    private record TestUser(Long id, String account, String password, String email) {
    }
}

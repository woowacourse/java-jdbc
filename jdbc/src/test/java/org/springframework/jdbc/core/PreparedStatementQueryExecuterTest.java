package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PreparedStatementQueryExecuterTest {

    @Mock
    private PreparedStatement pstmt;

    @Mock
    private RowMapper<TestObject> rowMapper;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void executeQuery_메서드를_실행하고_객체_리스트를_반환한다() throws SQLException {
        // given
        given(pstmt.executeQuery())
                .willReturn(resultSet);
        given(resultSet.next())
                .willReturn(true, false);
        given(rowMapper.mapRow(resultSet))
                .willReturn(new TestObject());

        PreparedStatementQueryExecuter<TestObject> executer = new PreparedStatementQueryExecuter<>(rowMapper);

        // when
        List<TestObject> results = executer.execute(pstmt);

        // then
        assertThat(results).hasSize(1);

        then(pstmt)
                .should(times(1))
                .executeQuery();
    }

    private static class TestObject {
    }
}

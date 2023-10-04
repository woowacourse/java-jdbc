package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PreparedStatementUpdateExecuterTest {

    @Mock
    private PreparedStatement pstmt;

    private AutoCloseable openedMock;

    @BeforeEach
    void setUp() {
        openedMock = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void afterAll() throws Exception {
        openedMock.close();
    }

    @Test
    void executeUpdate_메서드를_실행하고_affectedRows를_반환한다() throws SQLException {
        // given
        given(pstmt.executeUpdate())
                .willReturn(1);

        PreparedStatementUpdateExecuter executer = new PreparedStatementUpdateExecuter();

        // when
        Integer affectedRows = executer.execute(pstmt);

        // then
        assertThat(affectedRows).isEqualTo(1);

        then(pstmt)
                .should(times(1))
                .executeUpdate();
    }
}

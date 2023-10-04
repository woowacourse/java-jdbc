package org.springframework.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SetPreparedStatementMakerTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement pstmt;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void Parameter가_설정된_PreparedStatement를_만든다() throws SQLException {
        // given
        String sql = "INSERT INTO samples (param1, param2) VALUES (?, ?)";
        Object[] args = {"param1", "param2"};

        given(conn.prepareStatement(sql))
                .willReturn(pstmt);

        SetPreparedStatementMaker maker = new SetPreparedStatementMaker(sql, args);

        // when
        maker.makePreparedStatement(conn);

        // then
        then(pstmt)
                .should(times(2))
                .setObject(anyInt(), any());
    }
}

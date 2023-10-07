package org.springframework.transaction;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private AutoCloseable openedMock;

    @BeforeEach
    void setUp() throws SQLException {
        openedMock = MockitoAnnotations.openMocks(this);

        given(dataSource.getConnection())
                .willReturn(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        openedMock.close();
    }

    @Test
    void 예외가_발생하지_않으면_커밋된다() throws SQLException {
        // given
        TransactionManager transactionManager = new TransactionManager(dataSource);

        // when
        transactionManager.execute(conn -> {});

        // then
        then(connection)
                .should(times(1))
                .commit();
    }

    @Test
    void 예외가_발생하면_롤백된다() throws SQLException {
        // given
        TransactionManager transactionManager = new TransactionManager(dataSource);

        // when
        transactionManager.execute(conn -> {throw new SQLException();});

        // then
        then(connection)
                .should(times(1))
                .rollback();
    }
}

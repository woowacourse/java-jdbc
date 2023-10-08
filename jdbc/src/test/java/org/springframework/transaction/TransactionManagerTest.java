package org.springframework.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.dao.DataAccessException;

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
        TransactionExecuter transactionExecuter = conn -> {
        };

        // when
        transactionManager.execute(transactionExecuter);

        // then
        then(connection)
                .should(times(1))
                .commit();
        then(connection)
                .should(times(1))
                .close();
    }

    @Test
    void 예외가_발생하면_롤백된다() throws SQLException {
        // given
        TransactionManager transactionManager = new TransactionManager(dataSource);
        TransactionExecuter transactionExecuter = conn -> {
            throw new SQLException();
        };

        // expect
        assertThatThrownBy(() -> transactionManager.execute(transactionExecuter))
                .isInstanceOf(DataAccessException.class);

        then(connection)
                .should(times(1))
                .rollback();
        then(connection)
                .should(times(1))
                .close();
    }
}

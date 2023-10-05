package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionTemplateTest {

    private TransactionTemplate sut;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        sut = new TransactionTemplate(dataSource);
    }

    @Test
    void 입력받은_TransactionCallback에서_예외가_발생하면_롤백을_실행한다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        final TransactionCallback throwException = () -> {
            throw new IllegalArgumentException();
        };

        // when
        assertThatThrownBy(() -> sut.execute(throwException))
                .isInstanceOf(DataAccessException.class);

        // then
        then(connection)
                .should(times(1))
                .rollback();
    }

    @Test
    void 메서드를_실행_후_커넥션을_닫는다() throws SQLException {
        // given
        final Connection connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        final TransactionCallback emptyTransactionCallback = () -> {
        };

        // when
        sut.execute(emptyTransactionCallback);

        // then
        then(connection)
                .should(times(1))
                .close();
    }
}

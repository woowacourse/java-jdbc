package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.transaction.support.TransactionSynchronizationManager.bindResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.getResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource;

import java.sql.Connection;
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
class TransactionSynchronizationManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private AutoCloseable openedMock;

    @BeforeEach
    void setUp() {
        openedMock = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openedMock.close();
    }

    @Test
    void 자원을_반환할_때_자원이_없으면_null_을_반환한다() {
        // when
        Connection resource = getResource(dataSource);

        // then
        assertThat(resource).isNull();
    }

    @Test
    void 자원을_반환할_때_자원이_있으면_자원을_반환한다() {
        // given
        bindResource(dataSource, connection);

        // when
        Connection resource = getResource(dataSource);

        // then
        assertThat(resource).isEqualTo(connection);
    }

    @Test
    void 자원을_등록한다() {
        // when
        bindResource(dataSource, connection);

        // then
        assertThat(getResource(dataSource)).isEqualTo(connection);
    }

    @Test
    void 자원을_해제할_때_자원이_존재하면_해제한다() {
        // given
        bindResource(dataSource, connection);

        // when
        unbindResource(dataSource);

        // then
        assertThat(getResource(dataSource)).isNull();
    }
}

package org.springframework.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionManager;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class ConnectionManagerTest {

    DataSource dataSource = mock(DataSource.class);

    @BeforeEach
    void setUp() throws Exception {
        given(dataSource.getConnection())
            .willReturn(mock(Connection.class));
    }

    @AfterEach
    void tearDown() {
        if (ConnectionManager.isConnectionEnable()) {
            ConnectionManager.releaseConnection();
        }
    }

    @Test
    void getConnection_호출하면_트랜잭션_false() {
        // given
        ConnectionManager.getConnection(dataSource);

        // when & then
        assertThat(TransactionManager.isTransactionEnable())
            .isFalse();
    }

    @Test
    void releaseConnection_호출하면_트랜잭션_종료() {
        // given
        TransactionManager.begin();
        ConnectionManager.getConnection(dataSource);

        // when
        ConnectionManager.releaseConnection();

        // then
        assertThat(TransactionManager.isTransactionEnable())
            .isFalse();
    }

    @Test
    void 커넥션을_얻지_않고_releaseConnection_하면_예외() {
        // when & then
        assertThatThrownBy(ConnectionManager::releaseConnection)
            .isInstanceOf(IllegalStateException.class);
    }
}

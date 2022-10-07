package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class UpdateExecutorTest {

    @Test
    void integer를_반환하는지_확인한다() throws SQLException {
        // given
        PreparedStatementStarter pss = mock(PreparedStatementStarter.class);
        when(pss.executeUpdate()).thenReturn(3);

        UpdateExecutor updateExecutor = new UpdateExecutor();

        // when
        Integer affectedQueryCount = updateExecutor.executePreparedStatement(pss);

        // then
        assertThat(affectedQueryCount).isEqualTo(3);
    }

}

package nextstep.datasource;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import nextstep.jdbc.JdbcDataSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DatabasePopulatorTest {

    private DataSource h2DataSource;

    @BeforeEach
    void setUp() {
        h2DataSource = JdbcDataSourceBuilder.create(DataSourceType.H2)
                .url("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;")
                .user("")
                .password("")
                .build();
    }

    @DisplayName("DataSource 와 URL 을 이용하여 쿼리를 실행한다.")
    @Test
    void execute() {
        // given
        Logger logger = (Logger) LoggerFactory.getLogger(DatabasePopulator.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        DatabasePopulator databasePopulator = new DatabasePopulator(h2DataSource);
        URL url = getClass().getClassLoader().getResource("create.sql");
        String containsExpect = "create table if not exists test_table";

        // when
        databasePopulator.execute(url);

        // then
        List<ILoggingEvent> list = listAppender.list;
        assertThat(list).hasSize(1);
        assertThat(list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(query -> query.contains(containsExpect));
    }
}

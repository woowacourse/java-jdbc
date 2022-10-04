package nextstep.jdbc.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import nextstep.support.DataSourceConfig;
import org.junit.jupiter.api.Test;

class TransactionalProcessorTest {

    @Transactional
    static class TransactionalAnnotated {

        public void hello() {
            System.out.println("hello world!");
        }
    }

    @Test
    void createProxyByTypeAnnotated() {
        // given
        TransactionalProcessor processor = new TransactionalProcessor(DataSourceConfig.getInstance());
        // when
        TransactionalAnnotated object = processor.createProxy(TransactionalAnnotated.class);
        // then
        assertThat(object).isNotNull();
    }
}

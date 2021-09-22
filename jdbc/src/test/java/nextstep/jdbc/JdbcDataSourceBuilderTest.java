package nextstep.jdbc;

import nextstep.datasource.DataSourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class JdbcDataSourceBuilderTest {

    @DisplayName("DataSourceBuilder 를 이용하여 H2JdbcDataSource 를 생성한다.")
    @Test
    void buildH2DataSource() {
        // given
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;";
        String user = "";
        String password = "";

        // when, then
        assertThatCode(
                () -> JdbcDataSourceBuilder.create(DataSourceType.H2)
                        .url(url)
                        .user(user)
                        .password(password)
                        .build()
        ).doesNotThrowAnyException();
    }

    @DisplayName("DataSourceBuilder 를 이용하여 MysqlJdbcDataSource 를 생성한다.")
    @Test
    void buildMySqlDataSource() {
        // given
        String url = "jdbc:mysql://localhost:13306/jwp_dashboard?userSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
        String user = "root";
        String password = "root";

        // when, then
        assertThatCode(
                () -> JdbcDataSourceBuilder.create(DataSourceType.MYSQL)
                        .url(url)
                        .user(user)
                        .password(password)
                        .build()
        ).doesNotThrowAnyException();
    }
}

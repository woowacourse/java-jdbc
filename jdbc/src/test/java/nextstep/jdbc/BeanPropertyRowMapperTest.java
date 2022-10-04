package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class BeanPropertyRowMapperTest {

    @Test
    void BeanPropertyRowMapper의_매핑_테스트() throws SQLException {
        BeanPropertyRowMapper<Data> rowMapper = new BeanPropertyRowMapper<>(Data.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.getObject("id", Long.class)).thenReturn(1L);
        when(rs.getObject("name", String.class)).thenReturn("name");

        Data data = rowMapper.mapRow(rs, 0);

        assertAll(
                () -> assertThat(data.getId()).isEqualTo(1L),
                () -> assertThat(data.getName()).isEqualTo("name"),
                () -> verify(rs).getObject("id", Long.class),
                () -> verify(rs).getObject("name", String.class)
        );
    }

    static class Data {

        private final Long id;
        private final String name;

        public Data() {
            this(null, null);
        }

        public Data(final Long id, final String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
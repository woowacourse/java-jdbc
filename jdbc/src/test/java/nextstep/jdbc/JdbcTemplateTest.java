package nextstep.jdbc;

import nextstep.jdbc.config.TestDataSourceConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;
    private Book book;
    private final RowMapper<Book> bookRowMapper = rs -> new Book(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("author")
    );

    @BeforeAll
    static void beforeAll() {
        DataSource dataSource = TestDataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "create table books (id bigint primary key, name varchar(50), author varchar(50))";
        jdbcTemplate.update(sql);
    }

    @BeforeEach
    void setUp() {
        book = new Book(1L, "토비의 스프링", "이일민");
        String sql = "insert into books (id, name, author) values (?, ?, ?)";
        jdbcTemplate.update(sql, book.getId(), book.getName(), book.getAuthor());
    }

    @AfterEach
    void tearDown() {
        String sql = "delete from books";
        jdbcTemplate.update(sql);
    }

    @Test
    void update_메소드는_주어진_쿼리를_실행한다() {
        //given
        String insertSql = "insert into books (id, name, author) values (?, ?, ?)";
        String selectSql = "select * from books where id = ?";
        Book 클린코드 = new Book(2L, "클린코드", "로버트 C. 마틴");

        //when
        jdbcTemplate.update(insertSql, 클린코드.getId(), 클린코드.getName(), 클린코드.getAuthor());

        //then
        Book result = jdbcTemplate.queryForObject(selectSql, bookRowMapper, 클린코드.getId());
        assertThat(result).isEqualTo(클린코드);
    }

    @Test
    void queryForObject_메소드는_RowMapper에_해당하는_객체를_반환한다() {
        //given
        String sql = "select * from books where id = ?";

        //when
        Book result = jdbcTemplate.queryForObject(sql, bookRowMapper, book.getId());

        //then
        assertThat(result).isEqualTo(book);
    }


    @Test
    void queryForObject_메소드의_결과가_1개_이상이면_예외() {
        //given
        String insertSql = "insert into books (id, name, author) values (?, ?, ?)";
        String sql = "select * from books where author = ?";
        Book book1 = new Book(2L, "우테코에서 살아남기", "주노");
        Book book2 = new Book(3L, "서울에서 살아남기", "주노");
        jdbcTemplate.update(insertSql, book1.getId(), book1.getName(), book1.getAuthor());
        jdbcTemplate.update(insertSql, book2.getId(), book2.getName(), book2.getAuthor());

        //when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, bookRowMapper, "주노"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1개 이상의 결과가 존재합니다.");
    }

    @Test
    void queryForObject_메소드의_결과가_없으면_null_반환() {
        // given
        String sql = "select * from books where author = ?";

        // when
        Book result = jdbcTemplate.queryForObject(sql, bookRowMapper, "엄준식");

        // then
        assertThat(result).isNull();
    }

    @Test
    void query_메소드의_정상_실행() {
        // given
        String sql = "select * from books";

        // when
        List<Book> query = jdbcTemplate.query(sql, bookRowMapper);

        // then
        Assertions.assertThat(query).containsOnly(book);
    }

    @Test
    void query_메소드의_결과가_없으면_빈_배열_반환() {
        // given
        String sql = "select * from books where author = ?";

        // when
        List<Book> query = jdbcTemplate.query(sql, bookRowMapper, "엄준식");

        // then
        Assertions.assertThat(query).isEmpty();
    }

    private static class Book {
        private final Long id;
        private final String name;
        private final String author;

        public Book(Long id, String name, String author) {
            this.id = id;
            this.name = name;
            this.author = author;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAuthor() {
            return author;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Book)) return false;
            Book book = (Book) o;
            return Objects.equals(id, book.id) && Objects.equals(name, book.name) && Objects.equals(author, book.author);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, author);
        }
    }
}

package nextstep.jdbc;

public class JdbcUser {

    private Long id;
    private final String account;
    private String password;
    private final String email;

    public JdbcUser(final Long id, final String account, final String password, final String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public JdbcUser(final String account, final String password, final String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}

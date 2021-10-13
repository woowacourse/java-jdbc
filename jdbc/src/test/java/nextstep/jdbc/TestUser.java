package nextstep.jdbc;

public class TestUser {

    private Long id;
    private final String account;
    private final String password;
    private final String email;

    public TestUser(long id, String account, String password, String email) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public TestUser(String account, String password, String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public long getId() {
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}

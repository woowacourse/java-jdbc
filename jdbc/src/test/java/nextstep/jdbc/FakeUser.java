package nextstep.jdbc;

public class FakeUser {

    private Long id;
    private String account;
    private String password;
    private String email;

    public FakeUser(String account, String password, String email) {
        this.account = account;
        this.password = password;
        this.email = email;
    }

    public FakeUser(Long id, String account, String password, String email) {
        this.id = id;
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

package support;

import java.util.Objects;

public class TestUser {

    private Long id;
    private String name;
    private int age;

    private TestUser() {
    }

    public TestUser(Long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestUser testUser = (TestUser) o;
        return age == testUser.age && Objects.equals(id, testUser.id) && Objects.equals(name,
                testUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }
}

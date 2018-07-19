package ix.test.authentication;

import java.util.Objects;

/**
 * Created by katzelda on 3/3/17.
 */
public class UserResult {

    public final String userName;
    public final String email;
    public final boolean isActive;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserResult that = (UserResult) o;

        if (isActive != that.isActive) return false;
        if (!userName.equals(that.userName)) return false;
        return Objects.equals(userName, that.userName);

    }

    @Override
    public int hashCode() {
        int result = userName.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (isActive ? 1 : 0);
        return result;
    }

    public UserResult(String userName, boolean isActive, String email) {

        this.email = email;
        this.userName = userName;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "UserResult{" +
                "email='" + email + '\'' +
                ", userName='" + userName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

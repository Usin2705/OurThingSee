package metro.ourthingsee.POSTs;

/**
 * Created by giang on 8.2.2017.
 */

public class AuthenRequest {
    private String email;
    private String password;

    public AuthenRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthenRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

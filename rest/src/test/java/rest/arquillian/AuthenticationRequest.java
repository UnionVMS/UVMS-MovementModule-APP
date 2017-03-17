package rest.arquillian;

/**
 * Created by thofan on 2017-03-17.
 */
public class AuthenticationRequest {

    private String userName;
    private String password;


    public AuthenticationRequest(){}

    public AuthenticationRequest(String uid, String pwd){
        userName = uid;
        password = pwd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

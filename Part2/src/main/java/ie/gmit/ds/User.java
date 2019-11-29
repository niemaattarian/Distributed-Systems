package ie.gmit.ds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

//@JsonIgnoreProperties(value = { "password" })
public class User {

    @NotNull
    private Integer id;
    @NotBlank @Length(min=2, max=255)
    private String userName;;
    @Pattern(regexp=".+@.+\\.[a-z]+")
    private String email;
    private String password;

    public User() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String hashedPassword;
    private String salt;

    public User(Integer id, String userName, String email, String password) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        String[] hashResponse = new PasswordServiceClient("localhost", 50551).hash(id, password);
        this.hashedPassword = hashResponse[0];
        this.salt = hashResponse[1];
    }

    public User(Integer id, String userName, String email, String password, String hashedPassword, String salt) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getSalt() { return salt; }

    @Override
    public String toString() {
        return "User [id= " + id + ", user name= " + userName + ", email= " + email + ", hashedPassword= " + hashedPassword + ", salt= " + salt + "]";
    }
}
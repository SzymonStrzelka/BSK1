package bsk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class User{
    private String login;
    private byte[] password;
    private byte[] salt;

    public User(String login, byte[] password, byte[] salt) {
        this.login = login;
        this.password = password;
        this.salt = salt;
    }
    public User(){
        salt = new byte[8];
    };

    @XmlElement(name = "login")
    public String getLogin() {
        return login;
    }

    @XmlElement(name = "password")
    public byte[] getPassword() {
        return password;
    }

    public void setLogin(String login){
        this.login = login;
    }

    public void setPassword(byte[] password){
        this.password = password;
    }

    @XmlElement(name = "salt")
    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}

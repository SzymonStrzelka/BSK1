package bsk.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class User{

    @XmlElement(name = "login")
    private String login;
    @XmlElement(name = "password")
    private byte[] password;
    @XmlElement(name = "salt")
    private byte[] salt;
    @XmlElement(name = "pubKeyLocation")
    private String pubKeyLocation;
    @XmlElement(name = "pubKeyFormat")
    private String pubKeyFormat;
    @XmlElement(name = "pvtKeyLocation")
    private String pvtKeyLocation;
    @XmlElement(name = "pvtKeyFormat")
    private String pvtKeyFormat;

    public User(){
        salt = new byte[8];
    }
    public User(String login, byte[] password, byte[] salt) {
        this.login = login;
        this.password = password;
        this.salt = salt;
    }
}

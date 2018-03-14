package bsk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "users")
public class Users {

    private List<User> userList;

    public Users() {
        userList = new ArrayList<>();
    }

    public Users(List<User> userList) {
        this.userList = userList;
    }

    @XmlElements(@XmlElement(name = "user"))
    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public void addUser(User user) {
        userList.add(user);
    }
}

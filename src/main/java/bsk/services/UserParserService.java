package bsk.services;

import bsk.model.User;
import bsk.model.Users;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class UserParserService {

    private final String usersFilePath = "src/main/resources/users.xml";
    private Users users = new Users();

    public User getUser(String login) throws JAXBException {
        users = getUsersFromXml();
        for (User u : users.getUserList()){
            if (u.getLogin().equals(login))
                return u;
        }
        return null;
    }

    public void addUser(User user) throws JAXBException {
        users = getUsersFromXml();
        users.addUser(user);

        JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        //ZAPIS DO PLIKU
        jaxbMarshaller.marshal(users, new File(usersFilePath));
    }

    public List<User> getAllUsers() throws JAXBException {
        users = getUsersFromXml();
        return users.getUserList();
    }

    private Users getUsersFromXml() throws JAXBException {
        File file = new File(usersFilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        return (Users) jaxbUnmarshaller.unmarshal(file);
    }

}
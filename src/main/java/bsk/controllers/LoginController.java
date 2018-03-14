package bsk.controllers;

import bsk.model.User;
import bsk.services.PasswordEncryptionService;
import bsk.services.UserParserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class LoginController {

    @FXML
    TextField login;
    @FXML
    PasswordField password;

    private UserParserService userParserService;
    private PasswordEncryptionService passwordEncryptionService;

    public LoginController() {
        userParserService = new UserParserService();
        passwordEncryptionService = new PasswordEncryptionService();
    }

    public void verifyUser(ActionEvent event) {
        try {
            User user = userParserService.getUser(login.getText());
            if (user != null) {
                if (passwordEncryptionService.authenticate(password.getText(), user.getPassword(), user.getSalt()))
                    openMainWindow(event);
            }
        } catch (NoSuchAlgorithmException | JAXBException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void addUser(ActionEvent event) {
        try {
            byte[] salt = passwordEncryptionService.generateSalt();
            userParserService.addUser(new User(login.getText(),
                    passwordEncryptionService.getEncryptedPassword(password.getText(), salt), salt));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Account created!");
            alert.setContentText("You can now log in!");

            alert.showAndWait();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public void closeApplication(ActionEvent event) {
        // get a handle to the stage
        Stage stage = (Stage) password.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    private void openMainWindow(ActionEvent event) {
        Parent root;
        try {
            URL url = Paths.get("src/main/resources/fxml/MainWindow.fxml").toUri().toURL();
            root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle("Data encryption application");
            stage.setScene(new Scene(root, 520, 580));
            stage.show();
            // Hide this current window (if this is what you want)
            ((Node) (event.getSource())).getScene().getWindow().hide();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



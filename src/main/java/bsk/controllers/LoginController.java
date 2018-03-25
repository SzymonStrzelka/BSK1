package bsk.controllers;

import bsk.model.User;
import bsk.services.PasswordEncryptionService;
import bsk.services.PasswordValidator;
import bsk.services.RsaKeysService;
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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;


public class LoginController {

    @FXML
    private TextField login;
    @FXML
    private PasswordField password;

    private UserParserService userParserService;
    private PasswordEncryptionService passwordEncryptionService;
    private User currentUser;
    private RsaKeysService rsaKeysService;
    private final PasswordValidator passwordValidator;


    public LoginController() throws NoSuchAlgorithmException {
        userParserService = new UserParserService();
        passwordEncryptionService = new PasswordEncryptionService();
        rsaKeysService = new RsaKeysService();
        this.passwordValidator = new PasswordValidator(8, 30, 1, 1, 1);
    }

    public void logIn(ActionEvent event) {
        try {
            User user = userParserService.getUser(login.getText());
            if (user == null) {
                showError("User not found", "User with that name does not exist");
            } else {
                if (passwordEncryptionService.authenticate(password.getText(), user.getPassword(), user.getSalt())) {
                    currentUser = user;
                    openMainWindow(event);
                } else {
                    showError("Log in failure", "Bad credentials");
                }
            }
        } catch (NoSuchAlgorithmException | JAXBException | InvalidKeySpecException ex1) {
            ex1.printStackTrace();
            showError("Log in failure", "An error occurred while trying to log in");
        }
    }

    public void createAccount(ActionEvent event) {
        try {
            User user = userParserService.getUser(login.getText());
            if (user == null) {
                passwordValidator.validate(password.getText());

                byte[] salt = passwordEncryptionService.generateSalt();
                User newUser = new User(login.getText(),
                        passwordEncryptionService.getEncryptedPassword(password.getText(), salt), salt);
                //generate keys
                rsaKeysService.generateKeyPair(newUser);

                userParserService.addUser(newUser);

                showInfo("Account created!", "You can now log in!");
            } else {
                showError("Account creation failure", "User with that name already exists");
            }
        } catch (PasswordValidator.InvalidPasswordLengthException | PasswordValidator.InsufficientCharacterOccurrencesException ex1) {
            showError("Account creation failure", ex1.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            showError("Account creation failure", "An error occurred while trying to create account");
        }
    }

    public void closeApplication(ActionEvent event) {
        // get a handle to the stage
        Stage stage = (Stage) password.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    List<User> getAllUsers() {
        try {
            return userParserService.getAllUsers();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    User getCurrentUser() {
        return currentUser;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openMainWindow(ActionEvent event) {
        Parent root;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
            root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Data encryption application");
            stage.setScene(new Scene(root, 520, 580));
            stage.show();

            // Hide this current window (if this is what you want)
            ((Node) (event.getSource())).getScene().getWindow().hide();
            MainWindowController mainWindowController = fxmlLoader.getController();
            mainWindowController.setLoginController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



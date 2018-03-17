package bsk.controllers;

import bsk.crypto.encrypter.CipherMode;
import bsk.model.User;
import bsk.services.encryption.EncryptionService;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.CheckListView;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableArrayList;

public class MainWindowController {

    @FXML
    private ProgressBar decryptionProgressBar;
    @FXML
    private ProgressBar encryptionProgressBar;
    @FXML
    private Button decryptButton;
    @FXML
    private Button encryptButton;
    @FXML
    private Label decryptInputFileLabel;
    @FXML
    private Label decryptOutputFileLabel;
    @FXML
    private Label encryptInputFileLabel;
    @FXML
    private Label encryptOutputFileLabel;
    @FXML
    private ToggleGroup cipherModeGroup;
    @FXML
    private CheckListView<String> userList;

    private final FileChooser fileChooser = new FileChooser();
    private final Stage stage = new Stage();
    private final EncryptionService encryptionService = new EncryptionService();
    private LoginController loginController;
    private File encryptInFile;
    private File decryptInFile;
    private File encryptOutFile;
    private File decryptOutFile;
    private List<String> usernames;

    private User currentUser;

    void setLoginController(LoginController loginController) {
        this.loginController = loginController;
        init();
    }

    public void openFileToEncryption(ActionEvent event) {
        encryptInFile = fileChooser.showOpenDialog(stage);
        if (encryptInFile != null && encryptInFile.exists()) {
            encryptInputFileLabel.setText(encryptInFile.getName());
        } else {
            encryptInputFileLabel.setText("No input file selected");
        }
        fileChanged(encryptInFile, encryptOutFile, encryptButton);
    }

    public void chooseOutputFileEncryption(ActionEvent event) {
        encryptOutFile = fileChooser.showSaveDialog(stage);
        if (encryptOutFile != null)
            encryptOutputFileLabel.setText(encryptOutFile.getName());
        else
            encryptOutputFileLabel.setText("No output file selected");
        fileChanged(encryptInFile, encryptOutFile, encryptButton);
    }

    public void openFileToDecryption(ActionEvent event) {
        decryptInFile = fileChooser.showOpenDialog(stage);
        if (decryptInFile != null && decryptInFile.exists()) {
            decryptInputFileLabel.setText(decryptInFile.getName());
        } else {
            decryptInputFileLabel.setText("No input file selected");
        }
        fileChanged(decryptInFile, decryptOutFile,decryptButton);
    }

    public void chooseOutputFileDecryption(ActionEvent event) {
        decryptOutFile = fileChooser.showSaveDialog(stage);
        if (decryptOutFile != null) {
            decryptOutputFileLabel.setText(decryptOutFile.getName());
        } else {
            decryptOutputFileLabel.setText("No output file selected");
        }
        fileChanged(decryptInFile, decryptOutFile,decryptButton);
    }

    public void encrypt(ActionEvent event) {
        encryptionService.encrypt(encryptInFile, encryptOutFile, getSelectedMode(), getRecipients(),
                progress -> encryptionProgressBar.setProgress(progress),
                e -> {
                    encryptionProgressBar.setProgress(0);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Encryption error", ButtonType.OK);
                    alert.showAndWait();
                },
                () -> {
                    encryptionProgressBar.setProgress(1);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Encryption success", ButtonType.OK);
                    alert.showAndWait();
                });

    }

    public void decrypt(ActionEvent event) {
        encryptionService.decrypt(decryptInFile, decryptOutFile, currentUser,
                progress -> decryptionProgressBar.setProgress(progress),
                e -> {
                    decryptionProgressBar.setProgress(0);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Decryption error", ButtonType.OK);
                    alert.showAndWait();
                },
                () -> {
                    decryptionProgressBar.setProgress(1);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Decryption success", ButtonType.OK);
                    alert.showAndWait();
                });
    }

    private void fileChanged(File inFile, File outFile, Button button) {
        boolean disabled = inFile == null || !inFile.exists() || outFile == null;
        button.setDisable(disabled);
    }

    private CipherMode getSelectedMode() {
        return CipherMode.valueOf((String) cipherModeGroup.getSelectedToggle().getUserData());
    }

    private ObservableList<String> getObservableList(List<String> usernames) {
        ObservableList<String> strings = observableArrayList();
        strings.addAll(usernames.stream().
                filter(s -> !s.equals(currentUser.getLogin())).collect(Collectors.toList()));
        return  strings;
    }

    private void init() {
        currentUser = loginController.getCurrentUser();
        usernames = loginController.getUsernames();
        userList.setItems(getObservableList(usernames));
    }
    private List<String> getRecipients(){
        return Stream.concat(Stream.of(currentUser.getLogin()),
                userList.getCheckModel().getCheckedItems().stream()).collect(Collectors.toList());
    }
}

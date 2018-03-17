package bsk.controllers;

import bsk.crypto.encrypter.CipherMode;
import bsk.model.User;
import bsk.services.encryption.EncryptionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Collections;
import java.lang.reflect.Array;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainWindowController {
    private final EncryptionService encryptionService;

    public MainWindowController() {
        this.encryptionService = new EncryptionService();
    }

    @FXML
    private Label statusLabelEncryption;
    @FXML
    private RadioButton ECB;
    @FXML
    private RadioButton CBC;
    @FXML
    private RadioButton CFB;
    @FXML
    private RadioButton OFB;
    @FXML
    private ProgressBar decryptionProgressBar;
    @FXML
    private ProgressBar encryptionProgressBar;
    @FXML
    private Button decryptButton;
    @FXML
    private Label decryptInputFileLabel;
    @FXML
    private Label decryptOutputFileLabel;

    private final FileChooser fileChooser = new FileChooser();
    private Stage stage;
    private File encryptInFile;
    private File decryptInFile;
    private File encryptOutFile;
    private File decryptOutFile;
    @FXML
    private CipherMode cipherMode;

    private User currentUser = new User("januszzzzzzzzzzzzzzzzzz", "password".getBytes(), "dddddddddddddddd".getBytes());

    public void initialize(Stage stage) {
        this.stage = stage;
    }

    public void openFileToEncryption(ActionEvent event) {
        encryptInFile = fileChooser.showOpenDialog(stage);
        if (encryptInFile != null) {
            statusLabelEncryption.setText("Opened file " + encryptInFile.getName());
        }
    }

    public void chooseOutputFileEncryption(ActionEvent event) {
        encryptOutFile = fileChooser.showSaveDialog(stage);
        if (encryptOutFile != null) {
            statusLabelEncryption.setText("Encrypted file will be saved as " + encryptOutFile.getName());
        }
    }

    public void openFileToDecryption(ActionEvent event) {
        decryptInFile = fileChooser.showOpenDialog(stage);
        if (decryptInFile != null && decryptInFile.exists()) {
            decryptInputFileLabel.setText(decryptInFile.getName());
        } else {
            decryptInputFileLabel.setText("No input file selected");
        }
        onDecryptionFilesChanged();
    }

    public void chooseOutputFileDecryption(ActionEvent event) {
        decryptOutFile = fileChooser.showSaveDialog(stage);
        if (decryptOutFile != null) {
            decryptOutputFileLabel.setText(decryptOutFile.getName());
        } else {
            decryptOutputFileLabel.setText("No output file selected");
        }
        onDecryptionFilesChanged();
    }

    private void onDecryptionFilesChanged() {
        boolean disabled = decryptInFile == null || !decryptInFile.exists() || decryptOutFile == null;
        decryptButton.setDisable(disabled);

    }

    public void encrypt(ActionEvent event) {
        encryptionService.encrypt(encryptInFile, encryptOutFile, CipherMode.CBC, Collections.singletonList(currentUser),
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

    private CipherMode getSelectedRadio() {
        RadioButton radioButtons[] = {ECB, CBC, CFB, OFB};
        for (RadioButton rb : radioButtons) {
            if (rb.isSelected())
                return CipherMode.valueOf(rb.getId());
        }
        return null;
    }
}

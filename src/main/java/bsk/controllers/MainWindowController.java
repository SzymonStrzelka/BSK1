package bsk.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainWindowController {

    @FXML
    private Label statusLabelEncryption;
    @FXML
    private Label statusLabelDecryption;

    private final FileChooser fileChooser = new FileChooser();
    private Stage stage;
    private File encryptInFile;
    private File decryptInFile;
    private File encryptOutFile;
    private File decryptOutFile;

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
            statusLabelEncryption.setText("Decrypted file will be saved as " + encryptOutFile.getName());
        }
    }

    public void openFileToDecryption(ActionEvent event) {
        decryptInFile = fileChooser.showOpenDialog(stage);
        if (decryptInFile != null) {
            statusLabelDecryption.setText("Opened file " + decryptInFile.getName());
        }
    }

    public void chooseOutputFileDecryption(ActionEvent event) {
        decryptOutFile = fileChooser.showSaveDialog(stage);
        if (decryptOutFile != null) {
            statusLabelDecryption.setText("Decrypted file will be saved as " + decryptOutFile.getName());
        }
    }
}

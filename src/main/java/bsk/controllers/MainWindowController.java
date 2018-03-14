package bsk.controllers;

import bsk.crypto.Decrypter;
import bsk.crypto.Encrypter;
import bsk.crypto.SessionKeyGenerator;
import io.reactivex.Observable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainWindowController {
    private final SessionKeyGenerator generator;

    public MainWindowController() {
        this.generator = new SessionKeyGenerator();
    }

    @FXML
    private Label statusLabelEncryption;
    @FXML
    private Label statusLabelDecryption;
    @FXML
    private ProgressBar decryptionProgressBar;
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

    private SecretKey key;

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
        try (FileInputStream inputStream = new FileInputStream(encryptInFile);
             FileOutputStream outputStream = new FileOutputStream(encryptOutFile)) {
            key = generator.generate128BitKey();
            Encrypter encrypter = new Encrypter(key);

            int bytesRead;
            byte[] inputBytes = new byte[64];
            while ((bytesRead = inputStream.read(inputBytes)) != -1) {

                byte[] outputBytes = encrypter.encrypt(inputBytes, 0, bytesRead);
                if (outputBytes != null)
                    outputStream.write(outputBytes);
            }
            byte[] outputBytes = encrypter.end();
            if (outputBytes != null)
                outputStream.write(outputBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decrypt(ActionEvent event) {
        Observable
                .<Double>create(emitter -> {
                    try (FileInputStream inputStream = new FileInputStream(decryptInFile);
                         FileOutputStream outputStream = new FileOutputStream(decryptOutFile)) {
                        Decrypter decrypter = new Decrypter(key);

                        long totalBytesRead = 0;
                        int readBytes;
                        byte[] inputBytes = new byte[128];
                        while ((readBytes = inputStream.read(inputBytes)) != -1) {
                            totalBytesRead += readBytes;

                            byte[] outputBytes = decrypter.decrypt(inputBytes, 0, readBytes);
                            if (outputBytes != null)
                                outputStream.write(outputBytes);

                            double progress = (double) totalBytesRead / decryptInFile.length();
                            emitter.onNext(progress);
                        }
                        byte[] outputBytes = decrypter.end();
                        if (outputBytes != null)
                            outputStream.write(outputBytes);
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(
                        progress -> decryptionProgressBar.setProgress(progress),
                        e -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Decryption error", ButtonType.OK);
                            alert.showAndWait();
                        },
                        () -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Decryption success", ButtonType.OK);
                            alert.showAndWait();
                        });
    }
}

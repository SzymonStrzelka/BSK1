package bsk;

import bsk.controllers.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Paths;

public class RunApplication extends Application {

    private Stage stage;
    private MainWindowController mainWindowController;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = Paths.get("src/main/resources/fxml/Login.fxml").toUri().toURL();
        Pane mainWindow = FXMLLoader.load(url);
        Scene scene = new Scene(mainWindow);

        primaryStage.setTitle("Log in");
        primaryStage.setScene(scene);
        primaryStage.show();
        stage = primaryStage;
    }

    private void initialize(){
        mainWindowController = new MainWindowController();
    }
}

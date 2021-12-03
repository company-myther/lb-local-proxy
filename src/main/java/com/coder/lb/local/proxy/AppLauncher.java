package com.coder.lb.local.proxy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author zhuhf
 */
public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("hello-view.fxml").getURL());
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void start() {
        launch();
    }
}

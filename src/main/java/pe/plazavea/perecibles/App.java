package pe.plazavea.perecibles;

import javafx.application.Application;
import javafx.stage.Stage;
import pe.plazavea.perecibles.util.SceneManager;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        SceneManager.navigate("login");

        primaryStage.setTitle("Plaza Vea - Control de Perecibles");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

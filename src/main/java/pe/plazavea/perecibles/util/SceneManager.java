package pe.plazavea.perecibles.util;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pe.plazavea.perecibles.mock.MockData;
import pe.plazavea.perecibles.model.Usuario;

public final class SceneManager {

    private static final String FXML_PATH = "/fxml/%s.fxml";
    private static final String STYLESHEET_PATH = "/css/styles.css";

    private static Stage primaryStage;
    private static Usuario currentUser = MockData.getSupervisor();

    private SceneManager() {
    }

    public static void init(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Primary stage is required");
        }
        primaryStage = stage;
    }

    public static void navigate(String name) {
        ensureInitialized();

        Parent root = loadView(name);
        Scene scene = new Scene(root);
        stylesheetUrl().ifPresent(css -> scene.getStylesheets().add(css.toExternalForm()));
        primaryStage.setScene(scene);
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Usuario user) {
        if (user == null) {
            throw new IllegalArgumentException("Current user is required");
        }
        currentUser = user;
    }

    private static Parent loadView(String name) {
        URL viewUrl = SceneManager.class.getResource(FXML_PATH.formatted(name));
        if (viewUrl == null) {
            throw new IllegalArgumentException("FXML view not found: " + name);
        }

        try {
            return FXMLLoader.load(viewUrl);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load FXML view: " + name, exception);
        }
    }

    private static java.util.Optional<URL> stylesheetUrl() {
        return java.util.Optional.ofNullable(SceneManager.class.getResource(STYLESHEET_PATH));
    }

    private static void ensureInitialized() {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager.init(Stage) must be called first");
        }
    }
}

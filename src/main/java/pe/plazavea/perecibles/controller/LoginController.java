package pe.plazavea.perecibles.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.util.SceneManager;

public class LoginController {

    @FXML
    private Label userLabel;

    @FXML
    private void initialize() {
        Usuario user = SceneManager.getCurrentUser();
        userLabel.setText("%s %s - %s".formatted(user.getNombre(), user.getApellido(), user.getRol()));
    }
}

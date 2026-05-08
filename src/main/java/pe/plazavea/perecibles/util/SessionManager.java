package pe.plazavea.perecibles.util;

import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.config.SpringContext;
import pe.plazavea.perecibles.service.UsuarioServicio;
import pe.plazavea.perecibles.ui.Navigator;

public final class SessionManager {

    private static Usuario currentUser;

    private SessionManager() {
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static Usuario login(String email, String password) {
        currentUser = SpringContext.getBean(UsuarioServicio.class).login(email, password);
        return currentUser;
    }

    public static void setCurrentUser(Usuario user) {
        currentUser = user;
    }

    public static void logout(Navigator navigator) {
        currentUser = null;
        navigator.show("login");
    }
}

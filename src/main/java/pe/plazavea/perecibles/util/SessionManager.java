package pe.plazavea.perecibles.util;

import pe.plazavea.perecibles.model.Usuario;

public final class SessionManager {

    private static Usuario currentUser;

    private SessionManager() {
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Usuario user) {
        currentUser = user;
    }
}


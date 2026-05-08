package pe.plazavea.perecibles.ui;

public interface Navigator {
    void show(String screen);

    String getCurrentName();

    void refreshCurrentScreen();

    void toggleShortcutBar();
}

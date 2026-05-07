package pe.plazavea.perecibles.model;

import pe.plazavea.perecibles.enums.RolUsuario;

public class Usuario {

    private final int id;
    private final String nombre;
    private final String apellido;
    private final RolUsuario rol;

    public Usuario(int id, String nombre, String apellido, RolUsuario rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public RolUsuario getRol() {
        return rol;
    }
}

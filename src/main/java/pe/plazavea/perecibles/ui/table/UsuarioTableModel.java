package pe.plazavea.perecibles.ui.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.plazavea.perecibles.model.Usuario;

public final class UsuarioTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Usuario", "Nombre completo", "Rol", "Estado"};
    private final List<Usuario> usuarios = new ArrayList<>();

    public void setData(List<Usuario> newUsuarios) {
        usuarios.clear();
        usuarios.addAll(newUsuarios);
        fireTableDataChanged();
    }

    public Usuario getUsuarioAt(int row) {
        return usuarios.get(row);
    }

    @Override
    public int getRowCount() {
        return usuarios.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Usuario usuario = usuarios.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> usuario.getEmail();
            case 1 -> usuario.getNombreCompleto();
            case 2 -> usuario.getRol();
            case 3 -> usuario.isActivo() ? "Activo" : "Inactivo";
            default -> "";
        };
    }
}

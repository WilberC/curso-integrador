package pe.plazavea.perecibles.ui.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.plazavea.perecibles.model.Categoria;

public final class CategoriaTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Nombre", "Estado"};
    private final List<Categoria> categorias = new ArrayList<>();

    public void setData(List<Categoria> newCategorias) {
        categorias.clear();
        categorias.addAll(newCategorias);
        fireTableDataChanged();
    }

    public Categoria getCategoriaAt(int row) {
        return categorias.get(row);
    }

    @Override
    public int getRowCount() {
        return categorias.size();
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
        Categoria categoria = categorias.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> categoria.getNombre();
            case 1 -> categoria.isActivo() ? "Activa" : "Inactiva";
            default -> "";
        };
    }
}

package pe.plazavea.perecibles.ui.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.plazavea.perecibles.model.Categoria;
import pe.plazavea.perecibles.model.ProductoPerecible;

public final class ProductoTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Producto", "Categoría", "Unidad", "Estado"};
    private final List<ProductoPerecible> productos = new ArrayList<>();

    public void setData(List<ProductoPerecible> newProductos) {
        productos.clear();
        productos.addAll(newProductos);
        fireTableDataChanged();
    }

    public ProductoPerecible getProductoAt(int row) {
        return productos.get(row);
    }

    @Override
    public int getRowCount() {
        return productos.size();
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
        ProductoPerecible producto = productos.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> producto.getNombre();
            case 1 -> nombreCategoria(producto.getCategoriaEntity());
            case 2 -> producto.getUnidadMedida();
            case 3 -> producto.isActivo() ? "Activo" : "Inactivo";
            default -> "";
        };
    }

    private String nombreCategoria(Categoria categoria) {
        return categoria == null ? "" : categoria.getNombre();
    }
}

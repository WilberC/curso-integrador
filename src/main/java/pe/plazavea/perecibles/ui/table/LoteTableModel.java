package pe.plazavea.perecibles.ui.table;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.plazavea.perecibles.model.Lote;

public final class LoteTableModel extends AbstractTableModel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUMNS = {
            "Lote", "Producto", "Categoria", "Stock", "Vence", "Dias", "Ubicacion", "Estado"
    };

    private final List<Lote> lotes = new ArrayList<>();

    public LoteTableModel(List<Lote> lotes) {
        setData(lotes);
    }

    public void setData(List<Lote> newLotes) {
        lotes.clear();
        lotes.addAll(newLotes);
        fireTableDataChanged();
    }

    public Lote getLoteAt(int row) {
        return lotes.get(row);
    }

    @Override
    public int getRowCount() {
        return lotes.size();
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
        Lote lote = lotes.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> lote.getNumeroLote();
            case 1 -> lote.getProducto();
            case 2 -> lote.getCategoria();
            case 3 -> lote.getCantidadActual();
            case 4 -> DATE_FORMAT.format(lote.getFechaVencimiento());
            case 5 -> lote.getDiasParaVencer();
            case 6 -> lote.getUbicacion();
            case 7 -> lote.getEstado();
            default -> "";
        };
    }
}

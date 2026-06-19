package pe.plazavea.perecibles.ui.table;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pe.plazavea.perecibles.model.Alerta;

public final class AlertaTableModel extends AbstractTableModel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUMNS = {"Tipo", "Lote", "Dias", "Generada", "Estado", "Acciones"};

    private final List<Alerta> alertas = new ArrayList<>();

    public AlertaTableModel(List<Alerta> alertas) {
        setData(alertas);
    }

    public void setData(List<Alerta> newAlertas) {
        alertas.clear();
        alertas.addAll(newAlertas);
        fireTableDataChanged();
    }

    public Alerta getAlertaAt(int row) {
        return alertas.get(row);
    }

    @Override
    public int getRowCount() {
        return alertas.size();
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
        Alerta alerta = alertas.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> alerta.getTipoAlerta();
            case 1 -> alerta.getLoteNumero();
            case 2 -> alerta.getDiasParaVencer();
            case 3 -> DATE_FORMAT.format(alerta.getFechaGeneracion());
            case 4 -> alerta.getEstado();
            case 5 -> alerta.getEstado();
            default -> "";
        };
    }
}

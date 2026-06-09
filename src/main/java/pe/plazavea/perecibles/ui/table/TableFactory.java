package pe.plazavea.perecibles.ui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.TipoAlerta;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;
import pe.plazavea.perecibles.ui.component.StatusChip;

public final class TableFactory {

    private TableFactory() {
    }

    public static JTable loteTable(LoteTableModel model) {
        JTable table = baseTable(model);
        table.setDefaultRenderer(Object.class, new LoteCellRenderer(model));
        table.getColumnModel().getColumn(3).setCellRenderer(new NumericCellRenderer(model));
        table.getColumnModel().getColumn(4).setCellRenderer(new NumericCellRenderer(model));
        table.getColumnModel().getColumn(5).setCellRenderer(new NumericCellRenderer(model));
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionCellRenderer("V  R"));
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        return table;
    }

    public static JTable alertaTable(AlertaTableModel model) {
        JTable table = baseTable(model);
        table.setDefaultRenderer(Object.class, new DefaultLightCellRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new AlertTypeCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new AlertNumericCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer("Atender / Ignorar"));
        return table;
    }

    public static JTable simpleTable(javax.swing.table.TableModel model) {
        JTable table = baseTable(model);
        table.setDefaultRenderer(Object.class, new DefaultLightCellRenderer());
        return table;
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.SURFACE_SOFT);
        return scrollPane;
    }

    private static JTable baseTable(javax.swing.table.TableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(Theme.HAIRLINE);
        table.setBackground(Theme.SURFACE_SOFT);
        table.setForeground(Theme.BODY);
        table.setFont(Fonts.inter(Font.PLAIN, 13f));
        table.setSelectionBackground(Theme.TABLE_SELECTION);
        table.setSelectionForeground(Theme.INK);
        table.getTableHeader().setBackground(Theme.SURFACE_STRONG);
        table.getTableHeader().setForeground(Theme.MUTED);
        table.getTableHeader().setFont(Fonts.inter(Font.PLAIN, 11f));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        return table;
    }

    private static final class LoteCellRenderer extends DefaultLightCellRenderer {
        private final LoteTableModel model;

        private LoteCellRenderer(LoteTableModel model) {
            this.model = model;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            EstadoLote estado = model.getLoteAt(modelRow).getEstado();
            if (!selected) {
                label.setForeground(statusColor(estado));
            }
            if (column == 0) {
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, statusColor(estado)),
                        BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XS)
                ));
            }
            return label;
        }
    }

    private static final class NumericCellRenderer extends DefaultLightCellRenderer {
        private final LoteTableModel model;

        private NumericCellRenderer(LoteTableModel model) {
            this.model = model;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            int modelRow = table.convertRowIndexToModel(row);
            label.setFont(Fonts.mono(Font.PLAIN, 14f));
            label.setForeground(selected ? Theme.INK : statusColor(model.getLoteAt(modelRow).getEstado()));
            label.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_SM));
            return label;
        }
    }

    private static final class AlertNumericCellRenderer extends DefaultLightCellRenderer {
        private AlertNumericCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setFont(Fonts.mono(Font.PLAIN, 14f));
            label.setForeground(selected ? Theme.INK : Theme.WARNING);
            return label;
        }
    }

    private static final class StatusCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            if (value instanceof EstadoLote estado) {
                StatusChip chip = new StatusChip(estado);
                chip.setHorizontalAlignment(SwingConstants.CENTER);
                chip.setBackground(selected ? Theme.TABLE_SELECTION : Theme.SURFACE_SOFT);
                return chip;
            }
            return new JLabel(String.valueOf(value));
        }
    }

    private static final class AlertTypeCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            if (value instanceof TipoAlerta tipo) {
                EstadoLote estado = tipo == TipoAlerta.VENCIDO || tipo == TipoAlerta.CRITICA
                        ? EstadoLote.VENCIDO
                        : EstadoLote.PROXIMO_VENCER;
                StatusChip chip = new StatusChip(estado);
                chip.setText(labelFor(tipo));
                chip.setHorizontalAlignment(SwingConstants.CENTER);
                return chip;
            }
            return new JLabel(String.valueOf(value));
        }

        private String labelFor(TipoAlerta tipo) {
            return switch (tipo) {
                case VENCIDO -> "VENCIDO";
                case CRITICA -> "CRITICA";
                case PROXIMO_VENCER -> "ADVERTENCIA";
                case AVISO_ANTICIPADO -> "AVISO";
            };
        }
    }

    private static final class ActionCellRenderer extends DefaultLightCellRenderer {
        private final String labelText;

        private ActionCellRenderer(String labelText) {
            this.labelText = labelText;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setText(labelText);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setForeground(Theme.PRIMARY);
            label.setFont(Fonts.inter(Font.BOLD, 13f));
            return label;
        }
    }

    private static class DefaultLightCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            label.setFont(Fonts.inter(Font.PLAIN, 13f));
            label.setBackground(selected ? Theme.TABLE_SELECTION : Theme.SURFACE_SOFT);
            label.setForeground(selected ? Theme.INK : Theme.BODY);
            label.setBorder(BorderFactory.createEmptyBorder(0, Theme.SP_XS, 0, Theme.SP_XS));
            return label;
        }
    }

    private static java.awt.Color statusColor(EstadoLote estado) {
        return switch (estado) {
            case DISPONIBLE -> Theme.BODY;
            case PROXIMO_VENCER -> Theme.WARNING;
            case VENCIDO -> Theme.DANGER;
            case RETIRADO -> Theme.MUTED_STRONG;
        };
    }
}

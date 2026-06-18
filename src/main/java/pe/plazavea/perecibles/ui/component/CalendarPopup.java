package pe.plazavea.perecibles.ui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class CalendarPopup {

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final String[] WEEKDAY_LABELS = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
    private static final int COLUMNS = 7;
    private static final int CALENDAR_ROWS = 6;

    private final Component invoker;
    private final Consumer<LocalDate> onSelected;
    private LocalDate selectedDate;
    private YearMonth visibleMonth;
    private JPopupMenu popup;

    public CalendarPopup(Component invoker, LocalDate selectedDate, Consumer<LocalDate> onSelected) {
        this.invoker = invoker;
        this.selectedDate = selectedDate;
        this.visibleMonth = selectedDate == null ? YearMonth.now() : YearMonth.from(selectedDate);
        this.onSelected = onSelected;
    }

    public void show() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        popup = new JPopupMenu();
        popup.setFocusable(false);
        popup.setBorder(BorderFactory.createLineBorder(Theme.HAIRLINE));
        popup.add(calendarPanel());
        popup.show(invoker, 0, invoker.getHeight());
        if (invoker instanceof JComponent component) {
            SwingUtilities.invokeLater(component::requestFocusInWindow);
        }
    }

    private JPanel calendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, Theme.SP_XS));
        panel.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XS, Theme.SP_XS, Theme.SP_XS, Theme.SP_XS));
        panel.setBackground(Theme.SURFACE_SOFT);
        panel.add(monthHeader(), BorderLayout.NORTH);
        panel.add(daysGrid(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel monthHeader() {
        JPanel header = new JPanel(new BorderLayout(Theme.SP_XS, 0));
        header.setBackground(Theme.SURFACE_SOFT);
        JButton previous = smallButton("<");
        JButton next = smallButton(">");
        JLabel month = new JLabel(monthTitle(), SwingConstants.CENTER);
        month.setFont(Fonts.inter(Font.BOLD, 13f));
        month.setForeground(Theme.INK);
        previous.addActionListener(event -> {
            visibleMonth = visibleMonth.minusMonths(1);
            rebuildPopup();
        });
        next.addActionListener(event -> {
            visibleMonth = visibleMonth.plusMonths(1);
            rebuildPopup();
        });
        header.add(previous, BorderLayout.WEST);
        header.add(month, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        return header;
    }

    private JPanel daysGrid() {
        JPanel grid = new JPanel(new GridLayout(CALENDAR_ROWS + 1, COLUMNS, Theme.SP_XXS, Theme.SP_XXS));
        grid.setBackground(Theme.SURFACE_SOFT);
        for (String label : WEEKDAY_LABELS) {
            JLabel weekday = new JLabel(label, SwingConstants.CENTER);
            weekday.setFont(Fonts.inter(Font.BOLD, 11f));
            weekday.setForeground(Theme.MUTED);
            grid.add(weekday);
        }

        LocalDate firstDay = visibleMonth.atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate cursor = firstDay.minusDays(offset);
        for (int index = 0; index < CALENDAR_ROWS * COLUMNS; index++) {
            grid.add(dayButton(cursor));
            cursor = cursor.plusDays(1);
        }
        return grid;
    }

    private JButton dayButton(LocalDate date) {
        JButton button = smallButton(String.valueOf(date.getDayOfMonth()));
        boolean currentMonth = YearMonth.from(date).equals(visibleMonth);
        boolean selected = date.equals(selectedDate);
        button.setForeground(currentMonth ? Theme.INK : Theme.MUTED_STRONG);
        if (selected) {
            button.setBackground(Theme.PRIMARY);
            button.setForeground(Theme.ON_PRIMARY);
        }
        button.addActionListener(event -> {
            selectedDate = date;
            if (popup != null) {
                popup.setVisible(false);
            }
            onSelected.accept(date);
        });
        return button;
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Fonts.inter(Font.BOLD, 12f));
        button.setFocusable(false);
        button.setBackground(Theme.CANVAS);
        button.setForeground(Theme.INK);
        button.setBorder(BorderFactory.createEmptyBorder(Theme.SP_XXS, Theme.SP_XS, Theme.SP_XXS, Theme.SP_XS));
        return button;
    }

    private void rebuildPopup() {
        if (popup == null) {
            return;
        }
        popup.removeAll();
        popup.add(calendarPanel());
        popup.pack();
    }

    private String monthTitle() {
        String month = visibleMonth.getMonth().getDisplayName(TextStyle.FULL, ES);
        return month.substring(0, 1).toUpperCase(ES) + month.substring(1) + " " + visibleMonth.getYear();
    }
}

package pe.plazavea.perecibles.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class DatePickerField extends JPanel {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final String[] WEEKDAY_LABELS = {"Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do"};
    private static final int COLUMNS = 7;
    private static final int CALENDAR_ROWS = 6;

    private final JTextField textField = new JTextField();
    private final List<Runnable> changeListeners = new ArrayList<>();
    private final String placeholder;
    private LocalDate selectedDate;
    private YearMonth visibleMonth = YearMonth.now();
    private JPopupMenu popup;

    public DatePickerField(String placeholder) {
        this.placeholder = placeholder;
        setLayout(new BorderLayout(Theme.SP_XXS, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(132, 36));
        setMaximumSize(new Dimension(148, 36));
        configureTextField();
        add(textField, BorderLayout.CENTER);
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(selectedDate);
    }

    public void clear() {
        selectedDate = null;
        updateText();
        notifyChanged();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    private void configureTextField() {
        textField.setEditable(false);
        textField.setFocusable(false);
        textField.setFont(Fonts.inter(Font.PLAIN, 13f));
        textField.putClientProperty("JTextField.placeholderText", placeholder);
        textField.setToolTipText("Seleccione una fecha");
        textField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                showCalendar();
            }
        });
    }

    private void showCalendar() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        visibleMonth = selectedDate == null ? YearMonth.now() : YearMonth.from(selectedDate);
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Theme.HAIRLINE));
        popup.add(calendarPanel());
        popup.show(this, 0, getHeight());
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
            updateText();
            if (popup != null) {
                popup.setVisible(false);
            }
            notifyChanged();
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

    private void updateText() {
        textField.setText(selectedDate == null ? "" : DISPLAY_FORMAT.format(selectedDate));
    }

    private void notifyChanged() {
        changeListeners.forEach(Runnable::run);
    }
}

package pe.plazavea.perecibles.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pe.plazavea.perecibles.theme.Fonts;
import pe.plazavea.perecibles.theme.Theme;

public final class DatePickerField extends JPanel {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JTextField textField = new JTextField();
    private final List<Runnable> changeListeners = new ArrayList<>();
    private final String placeholder;
    private LocalDate selectedDate;

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

    public void setDate(LocalDate date) {
        selectedDate = date;
        updateText();
        notifyChanged();
    }

    public void clear() {
        selectedDate = null;
        updateText();
        notifyChanged();
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
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
        new CalendarPopup(this, selectedDate, date -> {
            selectedDate = date;
            updateText();
            notifyChanged();
        }).show();
    }

    private void updateText() {
        textField.setText(selectedDate == null ? "" : DISPLAY_FORMAT.format(selectedDate));
    }

    private void notifyChanged() {
        changeListeners.forEach(Runnable::run);
    }
}

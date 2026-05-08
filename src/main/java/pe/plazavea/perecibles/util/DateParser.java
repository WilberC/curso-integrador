package pe.plazavea.perecibles.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class DateParser {

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final Pattern HOY_MAS = Pattern.compile("hoy\\s*\\+\\s*(\\d+)(?:\\s*dias?)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern EN_SEMANAS = Pattern.compile("en\\s+(\\d+)\\s+semanas?", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter SHORT_DATE = new DateTimeFormatterBuilder()
            .appendPattern("d/M/uuuu")
            .toFormatter(ES);

    private DateParser() {
    }

    public static Optional<LocalDate> parse(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(text);
        if ("hoy".equals(normalized)) {
            return Optional.of(LocalDate.now());
        }
        var todayPlus = HOY_MAS.matcher(normalized);
        if (todayPlus.matches()) {
            return Optional.of(LocalDate.now().plusDays(Integer.parseInt(todayPlus.group(1))));
        }
        var weeks = EN_SEMANAS.matcher(normalized);
        if (weeks.matches()) {
            return Optional.of(LocalDate.now().plusWeeks(Integer.parseInt(weeks.group(1))));
        }
        if (normalized.startsWith("proximo ")) {
            return parseNextDay(normalized.substring("proximo ".length()));
        }
        try {
            return Optional.of(LocalDate.parse(normalized, SHORT_DATE));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    public static String formatLong(LocalDate date) {
        return "%d de %s de %d".formatted(
                date.getDayOfMonth(),
                date.getMonth().getDisplayName(TextStyle.FULL, ES),
                date.getYear()
        );
    }

    private static Optional<LocalDate> parseNextDay(String dayName) {
        for (DayOfWeek day : DayOfWeek.values()) {
            String display = normalize(day.getDisplayName(TextStyle.FULL, ES));
            if (display.equals(dayName)) {
                int days = day.getValue() - LocalDate.now().getDayOfWeek().getValue();
                return Optional.of(LocalDate.now().plusDays(days <= 0 ? days + 7 : days));
            }
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(ES)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }
}

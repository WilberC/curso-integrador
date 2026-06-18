package pe.plazavea.perecibles.util;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class DateParser {

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final Pattern PLUS_DAYS = Pattern.compile("\\+\\s*(\\d+)(?:\\s*dias?)?");
    private static final Pattern PLUS_MONTHS = Pattern.compile("\\+\\s*(\\d+)\\s+mes(?:es)?");
    private static final Pattern DAYS_ONLY = Pattern.compile("(\\d+)(?:\\s*dias?)?");
    private static final Pattern MONTHS_ONLY = Pattern.compile("(\\d+)\\s+mes(?:es)?");
    private static final Pattern HOY_MAS = Pattern.compile("hoy\\s*\\+\\s*(\\d+)(?:\\s*dias?)?");
    private static final Pattern HOY_MAS_MESES = Pattern.compile("hoy\\s*\\+\\s*(\\d+)\\s+mes(?:es)?");
    private static final Pattern HOY_MAS_SEMANA = Pattern.compile("hoy\\s*\\+\\s*(\\d+)\\s+semanas?");
    private static final Pattern EN_SEMANAS = Pattern.compile("en\\s+(\\d+)\\s+semanas?");
    private static final Pattern EN_MESES = Pattern.compile("en\\s+(\\d+)\\s+meses?");
    private static final Pattern PROXIMO_DIA = Pattern.compile("proximo\\s+(lunes|martes|miercoles|jueves|viernes|sabado|domingo)");
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/uuuu", ES),
            DateTimeFormatter.ofPattern("dd-MM-uuuu", ES),
            DateTimeFormatter.ISO_LOCAL_DATE
    );
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", ES);

    private DateParser() {
    }

    public static Optional<LocalDate> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(input);

        if ("hoy".equals(normalized)) {
            return Optional.of(LocalDate.now());
        }
        if ("manana".equals(normalized)) {
            return Optional.of(LocalDate.now().plusDays(1));
        }
        if ("pasado manana".equals(normalized)) {
            return Optional.of(LocalDate.now().plusDays(2));
        }

        var todayPlusWeeks = HOY_MAS_SEMANA.matcher(normalized);
        if (todayPlusWeeks.matches()) {
            return Optional.of(LocalDate.now().plusWeeks(Integer.parseInt(todayPlusWeeks.group(1))));
        }

        var todayPlusMonths = HOY_MAS_MESES.matcher(normalized);
        if (todayPlusMonths.matches()) {
            return Optional.of(LocalDate.now().plusMonths(Integer.parseInt(todayPlusMonths.group(1))));
        }

        var todayPlusDays = HOY_MAS.matcher(normalized);
        if (todayPlusDays.matches()) {
            return Optional.of(LocalDate.now().plusDays(Integer.parseInt(todayPlusDays.group(1))));
        }

        var plusMonths = PLUS_MONTHS.matcher(normalized);
        if (plusMonths.matches()) {
            return Optional.of(LocalDate.now().plusMonths(Integer.parseInt(plusMonths.group(1))));
        }

        var plusDays = PLUS_DAYS.matcher(normalized);
        if (plusDays.matches()) {
            return Optional.of(LocalDate.now().plusDays(Integer.parseInt(plusDays.group(1))));
        }

        var monthsOnly = MONTHS_ONLY.matcher(normalized);
        if (monthsOnly.matches()) {
            return Optional.of(LocalDate.now().plusMonths(Integer.parseInt(monthsOnly.group(1))));
        }

        var daysOnly = DAYS_ONLY.matcher(normalized);
        if (daysOnly.matches()) {
            return Optional.of(LocalDate.now().plusDays(Integer.parseInt(daysOnly.group(1))));
        }

        var weeks = EN_SEMANAS.matcher(normalized);
        if (weeks.matches()) {
            return Optional.of(LocalDate.now().plusWeeks(Integer.parseInt(weeks.group(1))));
        }

        var months = EN_MESES.matcher(normalized);
        if (months.matches()) {
            return Optional.of(LocalDate.now().plusMonths(Integer.parseInt(months.group(1))));
        }

        var weekday = PROXIMO_DIA.matcher(normalized);
        if (weekday.matches()) {
            return parseDayOfWeek(weekday.group(1)).map(DateParser::nextWeekday);
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return Optional.of(LocalDate.parse(input.trim(), formatter));
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }

        return Optional.empty();
    }

    public static String formatLong(LocalDate date) {
        return LONG_DATE.format(date);
    }

    private static LocalDate nextWeekday(DayOfWeek target) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() != target) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static Optional<DayOfWeek> parseDayOfWeek(String dayName) {
        for (DayOfWeek day : DayOfWeek.values()) {
            String display = normalize(day.getDisplayName(TextStyle.FULL, ES));
            if (display.equals(dayName)) {
                return Optional.of(day);
            }
        }
        return Optional.empty();
    }

    private static String normalize(String value) {
        String decomposed = Normalizer.normalize(value.trim().toLowerCase(ES), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }
}

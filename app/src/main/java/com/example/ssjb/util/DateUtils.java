package com.example.ssjb.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private static final SimpleDateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat ISO_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_TIME = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat DAY_LABEL = new SimpleDateFormat("EEEE", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_TIME = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private DateUtils() {
    }

    public static String todayIso() {
        return ISO_DATE.format(Calendar.getInstance().getTime());
    }

    public static String nowIsoDateTime() {
        return ISO_DATE_TIME.format(Calendar.getInstance().getTime());
    }

    public static String last30DaysIso() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        return ISO_DATE.format(cal.getTime());
    }

    public static String formatDisplayDate(String isoDate) {
        Date parsed = parseDate(isoDate, ISO_DATE);
        return parsed == null ? safe(isoDate) : DISPLAY_DATE.format(parsed);
    }

    public static String formatDisplayDateTime(String isoDateTime) {
        Date parsed = parseDate(isoDateTime, ISO_DATE_TIME);
        return parsed == null ? safe(isoDateTime) : DISPLAY_DATE_TIME.format(parsed);
    }

    public static String formatDisplayTime(String isoDateTime) {
        Date parsed = parseDate(isoDateTime, ISO_DATE_TIME);
        return parsed == null ? safe(isoDateTime) : DISPLAY_TIME.format(parsed);
    }

    public static String dayLabelForIsoDate(String isoDate) {
        Date parsed = parseDate(isoDate, ISO_DATE);
        return parsed == null ? "" : DAY_LABEL.format(parsed);
    }

    public static String combineDateAndTime(String isoDate, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        try {
            Date date = ISO_DATE.parse(isoDate);
            if (date != null) {
                cal.setTime(date);
            }
        } catch (ParseException ignored) {
        }
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return ISO_DATE_TIME.format(cal.getTime());
    }

    private static Date parseDate(String value, SimpleDateFormat format) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            format.setLenient(false);
            return format.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}

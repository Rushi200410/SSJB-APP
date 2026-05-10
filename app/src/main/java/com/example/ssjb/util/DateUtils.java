package com.example.ssjb.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class DateUtils {
    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private DateUtils() {
    }

    public static String todayIso() {
        return ISO.format(Calendar.getInstance().getTime());
    }

    public static String last30DaysIso() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        return ISO.format(cal.getTime());
    }
}

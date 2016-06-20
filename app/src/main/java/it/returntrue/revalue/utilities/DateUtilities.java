/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Provides utilities to manipulate dates
 * */
public class DateUtilities {
    public static String format(long milliseconds) {
        Date date = new Date(milliseconds);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        DateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.UK);
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.UK);

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            return "Today " + timeFormatter.format(date);
        } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday " + timeFormatter.format(date);
        } else {
            return dateFormatter.format(date);
        }
    }
}
package com.alexeykovzel.insidr.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String DEFAULT_FORMAT = "yyyy-dd-MM";

    public Date parse(String date) {
        return parse(date, DEFAULT_FORMAT);
    }

    public Date parse(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            System.out.println("[ERROR] Could not parse date: " + e.getMessage());
            return null;
        }
    }
}

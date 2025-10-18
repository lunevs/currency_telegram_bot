package com.skillbox.cryptobot.utils;

import java.util.regex.Pattern;

public class PriceValidator {

    private static final Pattern POSITIVE_DOUBLE = Pattern.compile("^\\d*\\.?\\d+$");

    public static boolean isPositiveDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        return POSITIVE_DOUBLE.matcher(str.trim()).matches();
    }

}

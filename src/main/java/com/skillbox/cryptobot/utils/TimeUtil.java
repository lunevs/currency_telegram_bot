package com.skillbox.cryptobot.utils;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtil {

    @Value("${telegram.bot.notify.delay.unit}")
    private static String delayUnit;

    @Value("${telegram.bot.notify.delay.value}")
    private static String delayValue;

    public static LocalDateTime getTimeToCompare() {
        ChronoUnit unit = ChronoUnit.valueOf(delayUnit.toUpperCase());
        return LocalDateTime.now().minus(Long.parseLong(delayValue), unit);
    }

}

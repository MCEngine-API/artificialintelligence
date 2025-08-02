package io.github.mcengine.api.artificialintelligence.function.calling.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class for formatting time values in various time zones
 * and generating dynamic placeholder labels.
 */
public class FunctionCallingLoaderUtilTime {

    /**
     * Returns the current time in the specified {@link TimeZone}, formatted as "HH:mm:ss".
     *
     * @param timeZone The time zone to format the time in.
     * @return The current time string formatted in the specified time zone.
     */
    public static String getFormattedTime(TimeZone timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(timeZone);
        return sdf.format(new Date());
    }

    /**
     * Returns the current time in the specified time zone ID string (e.g., "Asia/Bangkok"),
     * formatted as "HH:mm:ss".
     *
     * @param zoneId The string ID of the time zone.
     * @return The current time string formatted in the specified time zone.
     */
    public static String getFormattedTime(String zoneId) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(zoneId));
        return sdf.format(new Date());
    }

    /**
     * Generates a placeholder label for a given UTC/GMT offset to be replaced later with time values.
     * Example: getZoneLabel("utc", -7, 30) => "{time_utc_minus_07_30}"
     *
     * @param prefix Either "utc" or "gmt", used as the label's namespace.
     * @param hour   The hour offset from GMT (can be negative).
     * @param minute The minute offset (usually 0, 30, or 45).
     * @return A placeholder label string in the format "{time_<prefix>_plus/minus_HH_MM}".
     */
    public static String getZoneLabel(String prefix, int hour, int minute) {
        String sign = hour >= 0 ? "plus" : "minus";
        int absHour = Math.abs(hour);
        return String.format("{time_%s_%s_%02d_%02d}", prefix, sign, absHour, minute);
    }
}

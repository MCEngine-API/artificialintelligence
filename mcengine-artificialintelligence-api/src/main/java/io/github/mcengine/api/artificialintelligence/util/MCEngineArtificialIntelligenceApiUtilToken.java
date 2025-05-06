package io.github.mcengine.api.artificialintelligence.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Utility class for handling token validation and expiration date parsing for MCEngineArtificialIntelligence.
 */
public class MCEngineArtificialIntelligenceApiUtilToken {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Parses an expiration date from a supported input.
     *
     * @param input The date input (String or Date).
     * @return The parsed Date object.
     * @throws ParseException If the input string cannot be parsed.
     * @throws IllegalArgumentException If the input type is unsupported.
     */
    public static Date parseExpirationDate(Object input) throws ParseException {
        if (input instanceof Date) {
            return (Date) input;
        } else if (input instanceof String) {
            return DATE_FORMAT.parse((String) input);
        } else {
            throw new IllegalArgumentException("Unsupported expiration date input: " + input);
        }
    }

    /**
     * Validates a token against the plugin name, secret key, and current date.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string to validate.
     * @param nowDateInput The current date or date string.
     * @return True if the token is valid and not expired; false otherwise.
     */
    public static boolean validateToken(String pluginName, String secretKey, String token, Object nowDateInput) {
        try {
            Date nowDate = parseExpirationDate(nowDateInput);

            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decodedPayload = new String(decodedBytes);

            String[] parts = decodedPayload.split(":");
            if (parts.length != 4) {
                return false;
            }

            String tokenPluginName = parts[0];
            String tokenSecretKey = parts[1];
            String expirationDateStr = parts[2];

            if (!tokenPluginName.equals(pluginName)) {
                return false;
            }

            if (!tokenSecretKey.equals(secretKey)) {
                return false;
            }

            Date expirationDate = DATE_FORMAT.parse(expirationDateStr);

            String todayStr = DATE_FORMAT.format(nowDate);
            Date todayDate = DATE_FORMAT.parse(todayStr);

            return !todayDate.after(expirationDate);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the expiration date from a token.
     *
     * @param pluginName The plugin name.
     * @param secretKey The secret key used for validation.
     * @param token The token string.
     * @return The extracted expiration date, or a date representing epoch time (0) if invalid.
     */
    public static Date extractExpirationDate(String pluginName, String secretKey, String token) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decodedPayload = new String(decodedBytes);

            String[] parts = decodedPayload.split(":");
            if (parts.length != 4) {
                return new Date(0L);
            }

            String tokenPluginName = parts[0];
            String tokenSecretKey = parts[1];
            String expirationDateStr = parts[2];

            if (!tokenPluginName.equals(pluginName) || !tokenSecretKey.equals(secretKey)) {
                return new Date(0L);
            }

            return DATE_FORMAT.parse(expirationDateStr);
        } catch (Exception e) {
            return new Date(0L);
        }
    }
}

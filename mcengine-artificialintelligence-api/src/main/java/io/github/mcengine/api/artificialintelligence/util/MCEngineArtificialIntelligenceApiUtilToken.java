package io.github.mcengine.api.artificialintelligence.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bukkit.plugin.Plugin;

/**
 * Utility class for handling token validation and expiration date parsing for MCEngineArtificialIntelligence.
 */
public class MCEngineArtificialIntelligenceApiUtilToken {

    private static String secretKey;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Initializes the token encryption utility by loading the secret key from plugin config.
     *
     * @param plugin The Bukkit plugin instance.
     */
    public static void initialize(Plugin plugin) {
        secretKey = plugin.getConfig().getString("token.secretKey", "").trim();

        if (secretKey.isEmpty()) {
            plugin.getLogger().severe("token.secretKey is missing from the config. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
    
        if (secretKey.length() != 16) {
            plugin.getLogger().severe("token.secretKey must be exactly 16 characters for AES-128 encryption. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Encrypts the given token using AES.
     *
     * @param token The plain text token.
     * @return The encrypted token as a Base64 string.
     */
    public static String encryptToken(String token) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the given encrypted token.
     *
     * @param encryptedToken The encrypted token as a Base64 string.
     * @return The decrypted plain text token.
     */
    public static String decryptToken(String encryptedToken) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedToken);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /* ---------- Used for plugin licensing ---------- */

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

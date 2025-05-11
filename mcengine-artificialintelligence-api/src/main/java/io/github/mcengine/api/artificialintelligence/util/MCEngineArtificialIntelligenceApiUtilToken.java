package io.github.mcengine.api.artificialintelligence.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bukkit.plugin.Plugin;

/**
 * Utility class for handling player tokens in MCEngineArtificialIntelligence.
 */
public class MCEngineArtificialIntelligenceApiUtilToken {

    /**
     * Secret key used for AES encryption/decryption.
     * Must be exactly 16 characters long (128 bits) for AES-128.
     * Loaded from the plugin configuration during initialization.
     */
    private static String secretKey;

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
     * Encrypts the given token using AES encryption in CBC mode with PKCS5 padding.
     * A random 16-byte Initialization Vector (IV) is generated for each encryption,
     * ensuring that encrypting the same input multiple times will result in different outputs.
     * The IV is prepended to the encrypted data before Base64 encoding.
     *
     * @param token The plain text token to encrypt.
     * @return The encrypted token as a Base64-encoded string, including the IV.
     */
    public static String encryptToken(String token) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[16];
            new java.security.SecureRandom().nextBytes(iv); // generate random IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));

            // prepend IV to encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts a Base64-encoded token that was encrypted using AES in CBC mode with PKCS5 padding.
     * The method expects the IV to be prepended to the encrypted payload.
     *
     * @param encryptedToken The encrypted token as a Base64-encoded string.
     * @return The decrypted plain text token, or null if decryption fails.
     */
    public static String decryptToken(String encryptedToken) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedToken);
            byte[] iv = Arrays.copyOfRange(combined, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

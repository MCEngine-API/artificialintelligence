package io.github.mcengine.api.artificialintelligence.util;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for checking updates from GitHub or GitLab repositories.
 */
public class MCEngineArtificialIntelligenceApiUtilUpdate {

    /**
     * Checks for plugin updates from GitHub or GitLab.
     *
     * @param plugin      The plugin instance.
     * @param gitPlatform The git platform ("github" or "gitlab").
     * @param org         The organization or user name.
     * @param repository  The repository name.
     * @param token       The API authentication token (can be null or "null" if not used).
     */
    public static void checkUpdate(Plugin plugin, String gitPlatform, String org, String repository, String token) {
        Logger logger = plugin.getLogger();
        switch (gitPlatform.toLowerCase()) {
            case "github":
                checkUpdateGitHub(plugin, org, repository, token);
                break;
            case "gitlab":
                checkUpdateGitLab(plugin, org, repository, token);
                break;
            default:
                logger.warning("Unknown platform: " + gitPlatform);
        }
    }

    /**
     * Checks for plugin updates from a GitHub repository.
     *
     * @param plugin        The plugin instance.
     * @param org           The GitHub organization or user name.
     * @param repository    The GitHub repository name.
     * @param githubToken   The GitHub API token (optional).
     */
    private static void checkUpdateGitHub(Plugin plugin, String org, String repository, String githubToken) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", org, repository);
        String downloadUrl = String.format("https://github.com/%s/%s/releases", org, repository);
        fetchAndCompareUpdate(plugin, apiUrl, downloadUrl, githubToken, "application/vnd.github.v3+json", false);
    }

    /**
     * Checks for plugin updates from a GitLab repository.
     *
     * @param plugin        The plugin instance.
     * @param org           The GitLab group or user name.
     * @param repository    The GitLab repository name.
     * @param gitlabToken   The GitLab API token (optional).
     */
    private static void checkUpdateGitLab(Plugin plugin, String org, String repository, String gitlabToken) {
        String apiUrl = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/releases", org, repository);
        String downloadUrl = String.format("https://gitlab.com/%s/%s/-/releases", org, repository);
        fetchAndCompareUpdate(plugin, apiUrl, downloadUrl, gitlabToken, "application/json", true);
    }

    /**
     * Fetches the latest release from the API and compares it with the current plugin version.
     * If an update is available, logs update information to the console.
     *
     * @param plugin       The plugin instance.
     * @param apiUrl       The API endpoint URL.
     * @param downloadUrl  The URL to the release download page.
     * @param token        The API token (optional).
     * @param acceptHeader The accept header for the request (e.g., GitHub requires a specific one).
     * @param jsonArray    Whether the response is a JSON array (true for GitLab).
     */
    private static void fetchAndCompareUpdate(Plugin plugin, String apiUrl, String downloadUrl, String token, String acceptHeader, boolean jsonArray) {
        Logger logger = plugin.getLogger();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
                con.setRequestMethod("GET");
                if (token != null && !token.trim().isEmpty() && !"null".equalsIgnoreCase(token.trim())) {
                    con.setRequestProperty("Authorization", "token " + token);
                }
                con.setRequestProperty("Accept", acceptHeader);
                con.setDoOutput(true);

                JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
                String latestVersion;

                if (jsonArray) {
                    var jsonArrayObj = JsonParser.parseReader(reader).getAsJsonArray();
                    latestVersion = jsonArrayObj.size() > 0 ? jsonArrayObj.get(0).getAsJsonObject().get("tag_name").getAsString() : null;
                } else {
                    var jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    latestVersion = jsonObject.get("tag_name").getAsString();
                }

                if (latestVersion == null) {
                    logger.warning("[UpdateCheck] Could not find release tag from API: " + apiUrl);
                    return;
                }

                String version = plugin.getDescription().getVersion();
                boolean changed = isUpdateAvailable(version, latestVersion);

                if (changed) {
                    List<String> updateMessages = new ArrayList<>();
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r §6A new update is available!");
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r Current version: §e" + version + " §r>> Latest: §a" + latestVersion);
                    updateMessages.add("§9[MCEngineArtificialIntelligence]§r Download: §b" + downloadUrl);

                    updateMessages.forEach(msg -> Bukkit.getConsoleSender().sendMessage(msg));
                } else {
                    logger.info("[MCEngineArtificialIntelligence] No updates found. You are running the latest version.");
                }
            } catch (Exception ex) {
                logger.warning("[UpdateCheck] [" + (apiUrl.contains("github") ? "GitHub" : "GitLab") + "] Could not check updates: " + ex.getMessage());
            }
        });
    }

    /**
     * Compares the current plugin version with the latest version to determine if an update is available.
     *
     * @param currentVersion The currently installed version.
     * @param latestVersion  The latest version from the API.
     * @return true if an update is available; false otherwise.
     */
    @SuppressWarnings("unused")
    private static boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        String[] lv = latestVersion.split("\\.");
        String[] cv = currentVersion.split("\\.");

        boolean changed = lv.length != cv.length;
        changed = changed || !lv[0].equals(cv[0]); // Major
        if (!changed && lv.length > 1 && cv.length > 1)
            changed = changed || !lv[1].equals(cv[1]); // Minor
        if (!changed && lv.length > 2 && cv.length > 2)
            changed = changed || !lv[2].equals(cv[2]); // Patch

        return changed;
    }
}

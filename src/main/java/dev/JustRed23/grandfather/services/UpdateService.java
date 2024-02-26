package dev.JustRed23.grandfather.services;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.utils.JarUtils;
import dev.JustRed23.jdautils.music.AudioManager;
import dev.JustRed23.stonebrick.net.NetworkManager;
import dev.JustRed23.stonebrick.service.Service;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class UpdateService extends Service {

    public boolean shouldRun() {
        return Bot.enabled && Bot.auto_update;
    }

    public long delayBetweenRuns() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    public void run() throws Exception {
        final JSONObject json = NetworkManager.get("https://api.github.com/repos/JustRed23/Grandfather/releases/latest")
                .blocking()
                .asJSONObject();

        final String latestTag = json.getString("tag_name");

        if (!App.version.gitHash().equals(latestTag)) {
            AudioManager.destroyAll();

            LOGGER.info("New version available: " + latestTag);

            NetworkManager.get(json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url"))
                    .blocking()
                    .asFile(JarUtils.getJarFile());

            LOGGER.info("Updated to version " + latestTag);
            System.exit(-2); // Pterodactyl panel detects this as a crash and will automatically try a restart
        }
    }
}

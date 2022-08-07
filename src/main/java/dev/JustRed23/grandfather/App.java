package dev.JustRed23.grandfather;

import dev.JustRed23.grandfather.command.handler.CommandHandler;
import dev.JustRed23.grandfather.event.BasicEventListener;
import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;
import dev.JustRed23.stonebrick.log.SBLogger;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

public class App extends Application {

    private static Logger LOGGER;

    private ShardManager shardManager;
    private DefaultShardManagerBuilder builder;

    protected void init() throws Exception {
        LOGGER = SBLogger.getLogger(Bot.name);
        FileStructure.discover(GFS.class);
        builder = DefaultShardManagerBuilder.createDefault(Bot.token)
                .setBulkDeleteSplittingEnabled(false)
                .setEnableShutdownHook(false)
                .setEnabledIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .disableCache(
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI
                )
                .enableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.ONLINE_STATUS
                )
                .setChunkingFilter(ChunkingFilter.ALL)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("to 1970's hits"));
    }

    protected void start() throws Exception {
        if (!Bot.enabled) {
            LOGGER.info("Bot is disabled. Exiting...");
            exit();
            return;
        }

        shardManager = builder.build();
        shardManager.addEventListener(new BasicEventListener());

        CommandHandler.init();
    }

    protected void stop() throws Exception {
        if (shardManager != null)
            shardManager.shutdown();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package dev.JustRed23.grandfather;

import dev.JustRed23.abcm.Config;
import dev.JustRed23.grandfather.command.AdminCommands;
import dev.JustRed23.grandfather.command.GeneralCommands;
import dev.JustRed23.grandfather.command.MusicCommands;
import dev.JustRed23.grandfather.services.UpdateService;
import dev.JustRed23.grandfather.stats.SongsPerGuild;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.Command;
import dev.JustRed23.jdautils.data.DataStore;
import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;
import dev.JustRed23.stonebrick.log.SBLogger;
import dev.JustRed23.stonebrick.version.GitVersion;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

public class App extends Application {

    public static Logger LOGGER;
    public static GitVersion version;
    private static ShardManager shardManager;
    private DefaultShardManagerBuilder builder;

    protected void init() {
        LOGGER = SBLogger.getLogger(Bot.name);
        version = GitVersion.fromFile(getClass().getClassLoader().getResourceAsStream("application.properties"));
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
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.SCHEDULED_EVENTS
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
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(getDefaultActivity());

        getServicePool().addService(UpdateService.class);

        //Create db cache
        DataStore.createCache(1000);

        //Load commands
        AdminCommands.register();
        GeneralCommands.register();
        MusicCommands.register();
    }

    public static Activity getDefaultActivity() {
        return Activity.watching("TV");
    }

    protected void start() {
        if (!Bot.enabled) {
            LOGGER.info("Bot is disabled. Exiting...");
            exit();
            return;
        }

        //Load stats
        SongsPerGuild.load();

        shardManager = builder.build();
        shardManager.addEventListener(JDAUtilities.getInstance().withDatabase().fileBased("grandfather-settings.db").listener());

        shardManager.getShards().forEach(jda -> jda.updateCommands().addCommands(Command.globalCommands).queue());
    }

    protected void stop() {
        if (shardManager == null || !Bot.enabled)
            return;

        //Save stats
        SongsPerGuild.save();

        //Copied from BotCommons
        shardManager.shutdown();
        shardManager.getShardCache().forEach((jda) -> {
            jda.getHttpClient().connectionPool().evictAll();
            jda.getHttpClient().dispatcher().executorService().shutdown();
        });
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static void main(String[] args) {
        Config.setDebug(true);
        launch(args);
    }
}

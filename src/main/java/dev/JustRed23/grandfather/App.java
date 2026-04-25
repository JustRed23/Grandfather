package dev.JustRed23.grandfather;

import dev.JustRed23.abcm.Config;
import dev.JustRed23.grandfather.command.AdminCommands;
import dev.JustRed23.grandfather.command.GeneralCommands;
import dev.JustRed23.grandfather.command.MusicCommands;
import dev.JustRed23.grandfather.services.UpdateService;
import dev.JustRed23.grandfather.stats.SongsPerGuild;
import dev.JustRed23.jdautils.Builder;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.Command;
import dev.JustRed23.jdautils.data.DataStore;
import dev.JustRed23.jdautils.music.impl.lavalink.LavalinkMusicManager;
import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;
import dev.JustRed23.stonebrick.log.SBLogger;
import dev.JustRed23.stonebrick.version.GitVersion;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
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

        LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(Bot.token));
        addNodes(client);

        Builder.Configuration config = JDAUtilities.getInstance()
                .withDatabase()
                    .fileBased("grandfather-settings.db")
                .withMusicManager()
                    .useImplementation(new LavalinkMusicManager(client))
                    .addListener(MusicCommands.getListener())
                    .build()
                .buildConfiguration();

        builder = config.configure(DefaultShardManagerBuilder.createDefault(Bot.token))
                .setBulkDeleteSplittingEnabled(false)
                .setEnableShutdownHook(false)
                .setEnabledIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .disableCache(
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.SOUNDBOARD_SOUNDS
                )
                .enableCache(
                        CacheFlag.VOICE_STATE
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

    //<editor-fold desc="Lavalink Nodes">
    private void addNodes(LavalinkClient client) {
        client.addNode(
                new NodeOptions.Builder()
                        .setName("Serenetia")
                        .setServerUri("https://lavalinkv4.serenetia.com:443")
                        .setPassword("https://seretia.link/discord")
                        .build()
        );

        client.addNode(
                new NodeOptions.Builder()
                        .setName("Jirayu")
                        .setServerUri("https://lavalink.jirayu.net:443")
                        .setPassword("youshallnotpass")
                        .build()
        );

        client.addNode(
                new NodeOptions.Builder()
                        .setName("Millohost")
                        .setServerUri("https://lava-v4.millohost.my.id:443")
                        .setPassword("https://discord.gg/mjS5J2K3ep")
                        .build()
        );

        client.addNode(
                new NodeOptions.Builder()
                        .setName("Triniumhost")
                        .setServerUri("https://lavalink-v4.triniumhost.com:443")
                        .setPassword("free")
                        .build()
        );
    }
    //</editor-fold>

    public static Activity getDefaultActivity() {
        return Activity.watching("TV");
    }

    protected void start() {
        if (!Bot.enabled) {
            LOGGER.info("Bot is disabled. Exiting...");
            exit();
            return;
        }

        LOGGER.info("Bot online, running version " + version.gitHash());

        //Load stats
        SongsPerGuild.load();

        shardManager = builder.build();
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

    static void main(String[] args) {
        Config.setDebug(true);
        launch(args);
    }
}

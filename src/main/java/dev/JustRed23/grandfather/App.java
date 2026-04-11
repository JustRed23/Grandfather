package dev.JustRed23.grandfather;

import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import dev.JustRed23.abcm.Config;
import dev.JustRed23.grandfather.command.AdminCommands;
import dev.JustRed23.grandfather.command.GeneralCommands;
import dev.JustRed23.grandfather.command.MusicCommands;
import dev.JustRed23.grandfather.services.InactivityService;
import dev.JustRed23.grandfather.services.UpdateService;
import dev.JustRed23.grandfather.stats.SongsPerGuild;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.Command;
import dev.JustRed23.jdautils.data.DataStore;
import dev.JustRed23.jdautils.music.AudioManager;
import dev.JustRed23.stonebrick.app.Application;
import dev.JustRed23.stonebrick.data.FileStructure;
import dev.JustRed23.stonebrick.log.SBLogger;
import dev.JustRed23.stonebrick.version.GitVersion;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
                .setAudioModuleConfig(new AudioModuleConfig()
                        .withDaveSessionFactory(new JDaveSessionFactory())
                )
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(getDefaultActivity());

        getServicePool().addService(UpdateService.class);
        getServicePool().addService(InactivityService.class);

        //Create db cache
        DataStore.createCache(1000);

        //Load commands
        AdminCommands.register();
        GeneralCommands.register();
        MusicCommands.register();

        //default youtube source manager is deprecated, use lavaplayer's instead
        AudioManager.registerDefaultRemoteSources = false;

        List<Client> clients = new ArrayList<>(List.of(YoutubeAudioSourceManager.DEFAULT_CLIENTS));
        clients.add(new Tv());

        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(false, clients.toArray(new Client[0]));
        youtube.useOauth2(Bot.youtube_refresh_token.isBlank() ? null : Bot.youtube_refresh_token, false);
        AudioManager.playerManager.registerSourceManager(youtube);
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

        LOGGER.info("Bot online, running version " + version.gitHash());

        //Load stats
        SongsPerGuild.load();

        shardManager = builder.build();
        shardManager.addEventListener(JDAUtilities.getInstance().withDatabase().fileBased("grandfather-settings.db").listener());

        shardManager.getShards().forEach(jda -> jda.updateCommands().addCommands(Command.globalCommands).queue());
    }

    protected void stop() {
        if (shardManager == null || !Bot.enabled)
            return;

        //Destroy audio players
        AudioManager.destroyAll();

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

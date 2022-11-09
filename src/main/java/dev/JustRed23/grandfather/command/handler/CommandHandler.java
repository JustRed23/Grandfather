package dev.JustRed23.grandfather.command.handler;

import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.Category;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.ICommand;
import dev.JustRed23.grandfather.utils.Settings;
import dev.JustRed23.grandfather.utils.btn.ButtonHandler;
import dev.JustRed23.grandfather.utils.msg.ReactionHandler;
import dev.JustRed23.stonebrick.log.SBLogger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class CommandHandler {

    private static final Logger LOGGER = SBLogger.getLogger(CommandHandler.class);

    private static final List<ICommand> commands = new ArrayList<>();
    private static final List<CommandData> commandData = new ArrayList<>();

    private static final Map<Guild, ReactionHandler> reactionHandlers = new ConcurrentHashMap<>();
    private static final Map<Guild, ButtonHandler> buttonHandlers = new ConcurrentHashMap<>();

    private CommandHandler() {}

    public static void init() {
        LOGGER.info("=====Initializing command handler=====");
        LOGGER.info("Initializing commands:");
        addCommands().forEach(iCommand -> LOGGER.info("\tCommand {} loaded successfully", iCommand.getName()));
        LOGGER.info("=====Command handler initialized=====");
    }

    private static List<ICommand> addCommands() {
        Reflections reflections = new Reflections(ICommand.class.getPackage().getName() + ".commands");
        reflections.getSubTypesOf(ICommand.class).forEach(aClass -> {
            try {
                if (!isInstantiable(aClass))
                    return;
                ICommand command = aClass.getConstructor().newInstance();
                addCommand(command);
            } catch (Exception e) {
                LOGGER.warn("\tCommand {} could not be loaded", aClass.getSimpleName(), e);
            }
        });
        return getCommands();
    }

    private static void addCommand(ICommand command) {
        commands.add(command);
        commandData.add(command.getCommandData());
    }

    public static void handle(MessageChannelUnion channel, String message, Event event) {
        if (event instanceof SlashCommandInteractionEvent slashCommandInteractionEvent) {
            handleSlashCommand(slashCommandInteractionEvent);
            return;
        }

        if (channel.getType().isGuild())
            handleMessageReceivedEvent(channel, message, (MessageReceivedEvent) event, false);
        else if (channel.getType().equals(ChannelType.PRIVATE))
            handleMessageReceivedEvent(channel, message, (MessageReceivedEvent) event, true);
    }

    private static void handleSlashCommand(SlashCommandInteractionEvent event) {
        ICommand command = getCommand(event.getName());

        if (command == null || event.getGuild() == null || !event.getChannel().canTalk())
            return;

        CommandContext context = new CommandContext()
                .setSlashCommandEvent(event);

        switch (CommandChecker.doChecks(command, event.getUser(), event.getChannel(), false)) {
            case COMMAND_NOT_FOUND -> {
                if (Bot.show_unknown_command_message)
                    Templates.command_not_found.format(Settings.getPrefix(event.getGuild())).embed(event);
            }
            case COMMAND_NO_PERMISSION -> Templates.command_no_permission.embed(event);
            case BOT_NO_PERMISSION -> Templates.bot_no_permission.embed(event);
            case CANNOT_DETECT_STATE -> Templates.Music.cannot_detect_state.embed(event);
            case USER_NOT_CONNECTED -> Templates.Music.user_not_connected.embed(event);
            case BOT_NOT_CONNECTED -> Templates.Music.bot_not_connected.embed(event);
            case IN_DIFFERENT_CHANNEL -> Templates.Music.in_different_channel.embed(event);
            case NOT_PLAYING -> Templates.Music.not_playing.embed(event);
            case EMPTY_QUEUE -> Templates.Music.empty_queue.embed(event);
            case SUCCESS -> command.execute(context);
        }
    }

    private static void handleMessageReceivedEvent(MessageChannelUnion channel, String message, MessageReceivedEvent event, boolean privateMessage) {
        boolean startedWithMention = false;
        String prefix = privateMessage ? Bot.prefix : Settings.getPrefix(channel.asGuildMessageChannel());

        String mentionMe = "<@" + event.getJDA().getSelfUser().getId() + ">";
        String mentionMeAlias = "<@!" + event.getJDA().getSelfUser().getId() + ">";

        if (message.startsWith(mentionMe)) {
            message = message.replace(mentionMe, "").trim();
            startedWithMention = true;
        } else if (message.startsWith(mentionMeAlias)) {
            message = message.replace(mentionMeAlias, "").trim();
            startedWithMention = true;
        }

        if (!startedWithMention && !message.startsWith(prefix))
            return;

        if (!channel.canTalk())
            return;

        if (!startedWithMention)
            message = message.replaceFirst("(?i)" + Pattern.quote(prefix), "");

        String[] split = message.split("\\s+");
        String invoke = split[0];
        ICommand command = getCommand(invoke);

        List<String> args = Arrays.asList(split).subList(1, split.length);

        CommandContext context = new CommandContext()
                .setMessageReceivedEvent(event, privateMessage);
        context.setArgs(args);

        switch (CommandChecker.doChecks(command, event.getAuthor(), channel, privateMessage)) {
            case COMMAND_NOT_FOUND -> {
                if (Bot.show_unknown_command_message)
                    Templates.command_not_found.format(prefix).embed(event);
            }
            case COMMAND_NO_PERMISSION -> Templates.command_no_permission.embed(event);
            case BOT_NO_PERMISSION -> Templates.bot_no_permission.embed(event);
            case CANNOT_DETECT_STATE -> Templates.Music.cannot_detect_state.embed(event);
            case USER_NOT_CONNECTED -> Templates.Music.user_not_connected.embed(event);
            case BOT_NOT_CONNECTED -> Templates.Music.bot_not_connected.embed(event);
            case IN_DIFFERENT_CHANNEL -> Templates.Music.in_different_channel.embed(event);
            case NOT_PLAYING -> Templates.Music.not_playing.embed(event);
            case EMPTY_QUEUE -> Templates.Music.empty_queue.embed(event);
            case SUCCESS -> {
                event.getChannel().sendTyping().queue();
                assert command != null;
                command.execute(context);
            }
        }
    }

    //GETTERS & BOOLEANS
    public static List<ICommand> getCommands() {
        return commands;
    }

    public static List<CommandData> getCommandData() {
        return commandData;
    }

    public static List<ICommand> getCommandsByCategory(Category category) {
        return commands.stream().filter(command -> command.getCategory().equals(category)).toList();
    }

    @Nullable
    public static ICommand getCommand(String name) {
        return commands.stream().filter(command -> command.getName().equalsIgnoreCase(name) || command.getAliases().contains(name) || command.getCommandData().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private static boolean isInstantiable(@NotNull Class<?> clz) {
        return !clz.isPrimitive() &&
                !Modifier.isAbstract(clz.getModifiers()) &&
                !clz.isInterface() &&
                !clz.isArray() &&
                !clz.isAssignableFrom(String.class) &&
                !clz.isAssignableFrom(Integer.class);
    }

    public static ReactionHandler getReactionHandler(Guild guild) {
        if (reactionHandlers.containsKey(guild))
            return reactionHandlers.get(guild);

        ReactionHandler reactionHandler = new ReactionHandler();
        reactionHandlers.put(guild, reactionHandler);
        return reactionHandler;
    }

    public static ButtonHandler getButtonHandler(Guild guild) {
        if (buttonHandlers.containsKey(guild))
            return buttonHandlers.get(guild);

        ButtonHandler handler = new ButtonHandler();
        buttonHandlers.put(guild, handler);
        return handler;
    }
}

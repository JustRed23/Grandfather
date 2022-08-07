package dev.JustRed23.grandfather.command.commands.admin;

import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultAdminCommand;
import dev.JustRed23.grandfather.template.Templates;
import dev.JustRed23.grandfather.utils.UserUtils;
import dev.JustRed23.grandfather.utils.btn.BetterButton;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jooq.tools.StringUtils;

import java.util.List;

public class KickCommand extends DefaultAdminCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        Guild guild = channel.getGuild();
        Member author = event.getMember();

        assert author != null;

        final List<String> args = context.getArgs();

        if (args.isEmpty()) {
            EmbedUtils.sendWarningEmbed(Templates.kick.mention_a_member, event);
            return;
        }

        Member member = UserUtils.getMemberFromMention(args.get(0), guild, event.getJDA());

        if (member == null) {
            EmbedUtils.sendErrorEmbed(Templates.kick.member_not_found, event);
            return;
        }

        if (!author.canInteract(member)) {
            EmbedUtils.sendErrorEmbed(Templates.kick.user_cannot_interact, event);
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();

        if (!selfMember.canInteract(member)) {
            EmbedUtils.sendWarningEmbed(Templates.kick.bot_cannot_interact, event);
            return;
        }

        String reason = StringUtils.join(args.stream().skip(1).toList(), " ");

        event.getChannel().sendMessage("Are you sure you want to kick " + member.getUser().getAsTag() + "?")
                .setActionRow(
                        new BetterButton().success("grandfather:kick:yes", "Yes")
                                .onEvent(guild, event.getAuthor(), trigger -> {}, complete -> {
                                    AuditableRestAction<Void> kick = guild.kick(member);

                                    if (!reason.isEmpty())
                                        kick.reason(reason);

                                    kick.queue(
                                            success -> complete.deferEdit().queue(hook -> hook.editOriginal(new MessageBuilder(Templates.kick.success.format(member.getUser().getAsTag())).setActionRows().build()).queue()),
                                            fail -> complete.deferEdit().queue(hook -> hook.editOriginal(new MessageBuilder(Templates.kick.fail.format(member.getUser().getAsTag(), fail.getMessage())).setActionRows().build()).queue())
                                    );
                                }).build(),

                        new BetterButton().danger("grandfather:kick:no", "No")
                                .onEvent(guild, event.getAuthor(), trigger -> {}, complete ->
                                        complete.deferEdit().queue(interactionHook -> interactionHook.deleteOriginal().queue())
                                ).build()
                ).queue();
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        Member author = event.getMember();
        GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
        Guild guild = channel.getGuild();

        Member member = event.getOption("user").getAsMember();
        final OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "";

        assert author != null;

        if (member == null) {
            EmbedUtils.sendErrorEmbed(Templates.kick.member_not_found, event);
            return;
        }

        if (!author.canInteract(member)) {
            EmbedUtils.sendErrorEmbed(Templates.kick.user_cannot_interact, event);
            return;
        }

        Member selfMember = event.getGuild().getSelfMember();

        if (!selfMember.canInteract(member)) {
            EmbedUtils.sendWarningEmbed(Templates.kick.bot_cannot_interact, event);
            return;
        }

        event.reply("Are you sure you want to kick " + member.getUser().getAsTag() + "?")
                .addActionRow(
                        new BetterButton().success("grandfather:kick:yes", "Yes")
                                .onEvent(guild, event.getUser(), trigger -> {}, complete -> {
                                    AuditableRestAction<Void> kick = guild.kick(member);

                                    if (!reason.isEmpty())
                                        kick.reason(reason);

                                    kick.queue(
                                            success -> complete.deferEdit().queue(hook -> hook.editOriginal(new MessageBuilder(Templates.kick.success.format(member.getUser().getAsTag())).setActionRows().build()).queue()),
                                            fail -> complete.deferEdit().queue(hook -> hook.editOriginal(new MessageBuilder(Templates.kick.fail.format(member.getUser().getAsTag(), fail.getMessage())).setActionRows().build()).queue())
                                    );
                                }).build(),

                        new BetterButton().danger("grandfather:kick:no", "No")
                                .onEvent(guild, event.getUser(), trigger -> {}, complete ->
                                        complete.deferEdit().queue(interactionHook -> interactionHook.deleteOriginal().queue())
                                ).build()
                ).queue();
    }

    public String getName() {
        return "kick";
    }

    public String getHelp() {
        return "Kicks the mentioned user";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp())
                .addOption(OptionType.USER, "user", "The user you want to kick", true)
                .addOption(OptionType.STRING, "reason", "The reason you want to kick the user (optional)")
                .setGuildOnly(true);
    }

    public List<Permission> getUserPermissions() {
        return List.of(Permission.KICK_MEMBERS);
    }

    public List<Permission> getBotPermissions() {
        return List.of(Permission.KICK_MEMBERS);
    }
}

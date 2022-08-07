package dev.JustRed23.grandfather.command.commands.general;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.command.Category;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultGuildCommand;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class AboutCommand extends DefaultGuildCommand {

    public void execute(CommandContext context) {
        JDA jda = context.getJDA();
        User owner = jda.retrieveUserById(Bot.owner_id).complete();
        SelfUser self = jda.getSelfUser();

        long uptimeMs = (System.currentTimeMillis() - Bot.start_time);
        String uptime = TimeUtils.millisToTime(uptimeMs);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.MAGENTA);
        builder.setAuthor(self.getName() + " " + App.version.getVersion(), "https://github.com/JustRed23/Grandfather/commit/" + App.version.gitHash());
        builder.setThumbnail(self.getAvatarUrl());
        builder.setFooter("Made by " + owner.getAsTag(), owner.getEffectiveAvatarUrl());

        builder.addField("Total guilds", "" + App.getShardManager().getGuildCache().size(), true);
        builder.addField("Total users", "" + App.getShardManager().getUserCache().size(), true);
        builder.addField("Uptime", uptime, false);
        builder.addField("Built", App.version.buildTime().toString(), false);

        EmbedUtils.sendEmbed(builder, context.getEvent());
    }

    public String getName() {
        return "about";
    }

    public String getHelp() {
        return "Shows info about the current bot version";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp());
    }

    public Category getCategory() {
        return Category.GENERAL;
    }

    public List<String> getAliases() {
        return Arrays.asList("version", "info");
    }
}

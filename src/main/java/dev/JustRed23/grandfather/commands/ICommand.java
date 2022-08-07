package dev.JustRed23.grandfather.commands;

import dev.JustRed23.grandfather.utils.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Collections;
import java.util.List;

public interface ICommand {

    void run(CommandContext context);

    String getName();
    String getHelp();
    CommandData getCommandData();
    Category getCategory();

    default List<Permission> getUserPermissions() {
        return Collections.emptyList();
    }
    default List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    default String getPrefix(Guild guild) {
        return Settings.getPrefix(guild);
    }

    default List<String> getAliases() {
        return Collections.emptyList();
    }
}

package dev.JustRed23.grandfather.bettertemplate;

import dev.JustRed23.grandfather.utils.EmojiUtils;

import static dev.JustRed23.grandfather.bettertemplate.TemplateType.*;

public final class Templates {

    private Templates() {}

    public static final FormattedTemplate
            command_no_permission = FormattedTemplate.from(new UnformattedTemplate("%s You do not have permission to do this", WARNING)),
            bot_no_permission = FormattedTemplate.from(new UnformattedTemplate("%s I do not have the permission to do this", WARNING));

    public static final UnformattedTemplate
            command_not_found = new UnformattedTemplate("%s Unknown command. To show a list of commands use `%shelp`", ERROR),
            added_to_guild = new UnformattedTemplate("Thank you for adding me to `%s`.\nTo view a list of useful commands use `%shelp`", INFO, false);

    public static final class Prefix {
        public static final FormattedTemplate
                provide_prefix = FormattedTemplate.from(new UnformattedTemplate("%s Please provide a new prefix", WARNING)),
                failed = FormattedTemplate.from(new UnformattedTemplate("%s Could not change the prefix. Make sure the prefix is between one and three characters", ERROR));

        public static final UnformattedTemplate
                same_prefix = new UnformattedTemplate("%s This prefix (%s) is currently the default prefix of this server", WARNING),
                success = new UnformattedTemplate("%s Changed the prefix to `%s`", INFO);
    }

    public static final class Music {
        public static final FormattedTemplate
                user_not_connected = FormattedTemplate.from(new UnformattedTemplate("%s You are not connected to a voice channel. Please connect to one before using this command", ERROR)),
                bot_not_connected = FormattedTemplate.from(new UnformattedTemplate("%s I am not currently in a channel", WARNING)),
                cannot_detect_state = FormattedTemplate.from(new UnformattedTemplate("%s Cannot detect your voice state", ERROR)),
                already_in_channel = FormattedTemplate.from(new UnformattedTemplate("%s I am already connected to a voice channel", WARNING)),
                in_different_channel = FormattedTemplate.from(new UnformattedTemplate("%s You have to be in the same channel as me to use this command", WARNING)),
                no_link = FormattedTemplate.from(new UnformattedTemplate("%s You have to provide a link or a search term", ERROR)),
                not_playing = FormattedTemplate.from(new UnformattedTemplate("%s The player is not playing", WARNING)),
                already_paused = FormattedTemplate.from(new UnformattedTemplate("%s The player is already paused", INFO)),
                song_playing = FormattedTemplate.from(new UnformattedTemplate("%s The player is currently playing a song or the queue is not empty", WARNING)),
                no_previous_tracks = FormattedTemplate.from(new UnformattedTemplate("%s There are no previous tracks", ERROR)),
                empty_queue = FormattedTemplate.from(new UnformattedTemplate("%s The queue is empty", INFO)),
                disconnected = new UnformattedTemplate("%s Successfully disconnected", INFO, false).format(EmojiUtils.Misc.WAVE_GOODBYE),
                paused = new UnformattedTemplate("%s The track has been paused", INFO, false).format(EmojiUtils.Music.PAUSE),
                resumed = new UnformattedTemplate("%s The track has been resumed", INFO, false).format(EmojiUtils.Music.PLAY),
                restart = new UnformattedTemplate("%s Replaying track", INFO, false).format(EmojiUtils.Music.REPEAT),
                previous_track = new UnformattedTemplate("%s Went back to the previous track", INFO, false).format(EmojiUtils.Music.PREV_TRACK),
                next_track = new UnformattedTemplate("%s Skipped", INFO, false).format(EmojiUtils.Music.NEXT_TRACK),
                blocked_user = new UnformattedTemplate("%s This user has been blocked from using music commands", ERROR, false).format(EmojiUtils.General.NO);

        public static final UnformattedTemplate
                joined = new UnformattedTemplate("%s Successfully joined %s `%s` and bound to %s", INFO, false).formatRaw(EmojiUtils.General.YES, EmojiUtils.Music.SPEAKER),
                no_matches = new UnformattedTemplate("%s No matches found for `%s`", INFO);
    }

    public static final class Kick {
        public static final FormattedTemplate
                mention_a_member = FormattedTemplate.from(new UnformattedTemplate("%s Please mention a member to kick", WARNING)),
                member_not_found = FormattedTemplate.from(new UnformattedTemplate("%s This user could not be found", ERROR)),
                user_cannot_interact = FormattedTemplate.from(new UnformattedTemplate("%s You do not have permission to kick that person", ERROR)),
                bot_cannot_interact = FormattedTemplate.from(new UnformattedTemplate("%s I cannot kick that person", WARNING));

        public static final UnformattedTemplate
                success = new UnformattedTemplate("%s `%s` has been kicked", INFO),
                fail = new UnformattedTemplate("%s `%s` could not be kicked: %s", ERROR);
    }

    public static final class Ban {
        public static final FormattedTemplate
                mention_a_member = FormattedTemplate.from(new UnformattedTemplate("%s Please mention a member to ban", WARNING)),
                member_not_found = FormattedTemplate.from(new UnformattedTemplate("%s This user could not be found", ERROR)),
                user_cannot_interact = FormattedTemplate.from(new UnformattedTemplate("%s You do not have permission to ban that person", ERROR)),
                bot_cannot_interact = FormattedTemplate.from(new UnformattedTemplate("%s I cannot ban that person", WARNING));

        public static final UnformattedTemplate
                success = new UnformattedTemplate("%s `%s` has been banned", INFO),
                fail = new UnformattedTemplate("%s `%s` could not be banned: %s", ERROR);
    }

    public static final class Clean {
        public static final FormattedTemplate
                provide_amount = FormattedTemplate.from(new UnformattedTemplate("%s Please provide an amount of messages to delete", WARNING)),
                invalid_amount = FormattedTemplate.from(new UnformattedTemplate("%s Please provide a valid amount of messages to delete (1-1000)", WARNING)),
                no_messages = FormattedTemplate.from(new UnformattedTemplate("%s There are no messages to delete", INFO));

        public static final UnformattedTemplate
                success = new UnformattedTemplate("%s Successfully deleted %s message(s)", INFO),
                fail = new UnformattedTemplate("%s Could not delete message(s): %s", ERROR);
    }

    public static final class Help {
        public static final UnformattedTemplate
                no_command_or_category = new UnformattedTemplate("%s No command or category found for `%s`", ERROR),
                no_command = new UnformattedTemplate("%s No command found for `%s`", ERROR);
    }
}

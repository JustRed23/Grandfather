package dev.JustRed23.grandfather.template;

import dev.JustRed23.grandfather.utils.EmojiUtils;

import static dev.JustRed23.grandfather.template.TemplateTypes.*;

public class Templates {

    private Templates() {}

    public static final Template 
            command_not_found = new Template("%s Unknown command. To show a list of commands use `%shelp`", ERROR),
            command_no_permission = new Template("%s You do not have permission to do this", WARNING),
            bot_no_permission = new Template("%s I do not have the permission to do this", WARNING),
            added_to_guild = new Template("Thank you for adding me to `%s`.\nTo view a list of useful commands use `%shelp`");

    public static final class prefix {
        public static final Template
                provide_prefix = new Template("%s Please provide a new prefix", WARNING),
                same_prefix = new Template("%s This prefix (%s) is currently the default prefix of this server", WARNING),
                success = new Template("%s Changed the prefix to `%s`", INFO),
                failed = new Template("%s Could not change the prefix. Make sure the prefix is between one and three characters", ERROR);
    }

    public static final class music {
        public static final Template
                user_not_connected = new Template("%s You are not connected to a voice channel. Please connect to one before using this command", ERROR),
                bot_not_connected = new Template("%s I am not currently in a channel", WARNING),
                cannot_detect_state = new Template("%s Cannot detect your voice state", ERROR),
                joined = new Template("%s Successfully joined %s `%s` and bound to %s", EmojiUtils.General.YES, EmojiUtils.Music.SPEAKER),
                disconnected = new Template("%s Successfully disconnected", EmojiUtils.Misc.WAVE_GOODBYE),
                already_in_channel = new Template("%s I am already connected to a voice channel", WARNING),
                in_different_channel = new Template("%s You have to be in the same channel as me to use this command", WARNING),
                no_link = new Template("%s You have to provide a link or a search term", ERROR),
                no_matches = new Template("%s No matches found for `%s`", INFO),
                not_playing = new Template("%s The player is not playing", WARNING),
                already_paused = new Template("%s The player is already paused", INFO),
                paused = new Template("%s The track has been paused", EmojiUtils.Music.PAUSE, INFO),
                resumed = new Template("%s The track has been resumed", EmojiUtils.Music.PLAY, INFO),
                song_playing = new Template("%s The player is currently playing a song or the queue is not empty", WARNING),
                no_previous_tracks = new Template("%s There are no previous tracks", ERROR),
                next_track = new Template("%s Skipped", EmojiUtils.Music.NEXT_TRACK, INFO),
                previous_track = new Template("%s Went back to the previous track", EmojiUtils.Music.PREV_TRACK, INFO),
                empty_queue = new Template("%s The queue is empty", INFO);
    }

    public static final class kick {
        public static final Template
                mention_a_member = new Template("%s Please mention a member to kick", WARNING),
                member_not_found = new Template("%s This user could not be found", ERROR),
                user_cannot_interact = new Template("%s You do not have permission to kick that person", ERROR),
                bot_cannot_interact = new Template("%s I cannot kick that person", WARNING),
                success = new Template("%s `%s` has been kicked", INFO),
                fail = new Template("%s `%s` could not be kicked: %s", ERROR);
    }

    public static final class ban {
        public static final Template
                mention_a_member = new Template("%s Please mention a member to ban", WARNING),
                member_not_found = new Template("%s This user could not be found", ERROR),
                user_cannot_interact = new Template("%s You do not have permission to ban that person", ERROR),
                bot_cannot_interact = new Template("%s I cannot ban that person", WARNING),
                success = new Template("%s `%s` has been banned", INFO),
                fail = new Template("%s `%s` could not be banned: %s", ERROR);
    }

    public static final class help {
        public static final Template
                no_command_or_category = new Template("%s No command or category found for `%s`", ERROR),
                no_command = new Template("%s No command found for `%s`", ERROR);
    }
}

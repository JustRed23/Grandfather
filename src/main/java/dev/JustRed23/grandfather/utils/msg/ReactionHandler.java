package dev.JustRed23.grandfather.utils.msg;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.concurrent.ConcurrentHashMap;

public class ReactionHandler {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, ReactionListener>> reactions;

    public ReactionHandler() {
        reactions = new ConcurrentHashMap<>();
    }

    public synchronized void addReactionListener(long guildId, Message message, ReactionListener handler) {
        addReactionListener(guildId, message, handler, true);
    }

    public synchronized void addReactionListener(long guildId, Message message, ReactionListener handler, boolean queue) {
        if (handler == null)
            return;

        if (message.getChannelType().equals(ChannelType.TEXT) && !PermissionUtil.checkPermission(message.getGuildChannel().asTextChannel(), message.getGuild().getSelfMember(), Permission.MESSAGE_ADD_REACTION))
            return;

        reactions.computeIfAbsent(guildId, aLong -> reactions.put(aLong, new ConcurrentHashMap<>()));
        if (!reactions.get(guildId).containsKey(message.getIdLong())) {
            for (String emote : handler.getEmotes()) {
                RestAction<Void> action = message.addReaction(Emoji.fromUnicode(emote));
                if (queue) action.queue(); else action.complete();
            }
        }
        reactions.get(guildId).put(message.getIdLong(), handler);
    }

    public synchronized void removeReactionListener(long guildId, long messageId) {
        if (!reactions.containsKey(guildId)) return;
        reactions.get(guildId).remove(messageId);
    }

    public synchronized void removeReactionListener(ReactionListener reactionListener) {
        long guildId = reactionListener.getMessage().getGuild().getIdLong();
        long messageId = reactionListener.getMessage().getIdLong();
        removeReactionListener(guildId, messageId);
    }

    /**
     * Handles the reaction
     *
     * @param channel   TextChannel of the message
     * @param messageId id of the message
     * @param userId    id of the user reacting
     * @param reaction  the reaction
     */
    public void handle(TextChannel channel, long messageId, long userId, MessageReaction reaction) {
        ReactionListener listener = reactions.get(channel.getGuild().getIdLong()).get(messageId);
        if (!listener.isActive() || listener.getExpiresInTimestamp() < System.currentTimeMillis()) {
            reactions.get(channel.getGuild().getIdLong()).remove(messageId);
        } else if ((listener.hasReaction(reaction.getEmoji().getName()))) {
            reactions.get(channel.getGuild().getIdLong()).get(messageId).updateLastAction();
            Message message = channel.retrieveMessageById(messageId).complete();
            if (listener.getUser() == null || listener.getUser().getIdLong() == userId)
                listener.react(reaction.getEmoji().getName(), message);
        }
        cleanCache();
    }

    /**
     * Do we have an event for a message?
     *
     * @param guildId   discord guild-id of the message
     * @param messageId id of the message
     * @return do we have a handler?
     */
    public boolean canHandle(long guildId, long messageId) {
        return reactions.containsKey(guildId) && reactions.get(guildId).containsKey(messageId);
    }

    public synchronized void removeGuild(long guildId) {
        reactions.remove(guildId);
    }

    /**
     * Delete expired handlers
     */
    public synchronized void cleanCache() {
        long now = System.currentTimeMillis();
        reactions.forEach((key, value) -> {
            value.values().removeIf(listener -> !listener.isActive() || listener.getExpiresInTimestamp() < now);
            if (value.values().isEmpty())
                reactions.remove(key);
        });
    }
}

package dev.JustRed23.grandfather.utils.msg;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ReactionListener {

    private final Map<String, Consumer<Message>> reactions;
    private final Message message;
    private Long expiresIn, lastAction;
    private boolean active;

    private User user;

    public ReactionListener(Message message) {
        this.message = message;
        reactions = new LinkedHashMap<>();
        active = true;
        lastAction = System.currentTimeMillis();
        expiresIn = TimeUnit.MINUTES.toMillis(5);
    }

    public boolean isActive() {
        return active;
    }

    public void disable() {
        this.active = false;
    }

    /**
     * The time after which this listener expires which is now + specified time
     * Defaults to now+5min
     *
     * @param timeUnit time units
     * @param time     amount of time units
     */
    public void setExpiresIn(TimeUnit timeUnit, long time) {
        expiresIn = timeUnit.toMillis(time);
    }

    /**
     * Check if this listener has specified emote
     *
     * @param emote the emote to check for
     * @return does this listener do anything with this emote?
     */
    public boolean hasReaction(String emote) {
        return reactions.containsKey(emote);
    }

    /**
     * React to the reaction :')
     *
     * @param emote   the emote used
     * @param message the message bound to the reaction
     */
    public void react(String emote, Message message) {
        if (hasReaction(emote)) reactions.get(emote).accept(message);
    }

    public Message getMessage() {
        return message;
    }

    /**
     * Register a consumer for a specified emote
     * Multiple emotes will result in overriding the old one
     *
     * @param emote    the emote to respond to
     * @param consumer the behaviour when emote is used
     */
    public void registerReaction(String emote, Consumer<Message> consumer) {
        reactions.put(emote, consumer);
    }

    /**
     * Register a consumer for a specified emote
     * Multiple emotes will result in overriding the old one
     *
     * @param emote    the emote to respond to
     * @param consumer the behaviour when emote is used
     * @param user     the user that can use this reaction
     */
    public void registerReaction(String emote, Consumer<Message> consumer, User user) {
        reactions.put(emote, consumer);
        this.user = user;
    }

    /**
     * @return list of all emotes used in this reaction listener
     */
    public Set<String> getEmotes() {
        return reactions.keySet();
    }

    /**
     * updates the timestamp when the reaction was last accessed
     */
    public void updateLastAction() {
        lastAction = System.currentTimeMillis();
    }

    /**
     * When does this reaction listener expire?
     *
     * @return timestamp in millis
     */
    public Long getExpiresInTimestamp() {
        return lastAction + expiresIn;
    }

    @Nullable
    public User getUser() {
        return user;
    }
}

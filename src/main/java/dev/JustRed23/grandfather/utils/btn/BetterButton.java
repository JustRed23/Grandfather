package dev.JustRed23.grandfather.utils.btn;

import dev.JustRed23.grandfather.command.handler.CommandHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BetterButton {

    private ButtonStyle style;
    private String id;
    private String label;
    private Emoji emoji;

    @Nonnull
    @CheckReturnValue
    public BetterButton primary(@Nonnull String id, @Nonnull String label) {
        Checks.notNull(id, "ID");
        Checks.notNull(label, "Label");
        this.style = ButtonStyle.PRIMARY;
        this.id = id;
        this.label = label;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton primary(@Nonnull String id, @Nonnull Emoji emoji) {
        Checks.notNull(id, "ID");
        Checks.notNull(emoji, "Emoji");
        this.style = ButtonStyle.PRIMARY;
        this.id = id;
        this.emoji = emoji;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton secondary(@Nonnull String id, @Nonnull String label) {
        Checks.notNull(id, "ID");
        Checks.notNull(label, "Label");
        this.style = ButtonStyle.SECONDARY;
        this.id = id;
        this.label = label;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton secondary(@Nonnull String id, @Nonnull Emoji emoji) {
        Checks.notNull(id, "ID");
        Checks.notNull(emoji, "Emoji");
        this.style = ButtonStyle.SECONDARY;
        this.id = id;
        this.emoji = emoji;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton success(@Nonnull String id, @Nonnull String label) {
        Checks.notNull(id, "ID");
        Checks.notNull(label, "Label");
        this.style = ButtonStyle.SUCCESS;
        this.id = id;
        this.label = label;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton success(@Nonnull String id, @Nonnull Emoji emoji) {
        Checks.notNull(id, "ID");
        Checks.notNull(emoji, "Emoji");
        this.style = ButtonStyle.SUCCESS;
        this.id = id;
        this.emoji = emoji;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton danger(@Nonnull String id, @Nonnull String label) {
        Checks.notNull(id, "ID");
        Checks.notNull(label, "Label");
        this.style = ButtonStyle.DANGER;
        this.id = id;
        this.label = label;
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton danger(@Nonnull String id, @Nonnull Emoji emoji) {
        Checks.notNull(id, "ID");
        Checks.notNull(emoji, "Emoji");
        this.style = ButtonStyle.DANGER;
        this.id = id;
        this.emoji = emoji;
        return this;
    }

    @Nonnull
    public Button link(@Nonnull String url, @Nonnull String label) {
        return Button.link(url, label);
    }

    @Nonnull
    public Button link(@Nonnull String url, @Nonnull Emoji emoji) {
        return Button.link(url, emoji);
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton onEvent(@Nonnull Guild guild, @Nonnull Consumer<ButtonInteractionEvent> onTrigger, @Nonnull Consumer<ButtonInteractionEvent> onComplete) {
        Checks.notNull(id, "ID");
        Checks.notNull(guild, "Guild");
        Checks.notNull(onTrigger, "ButtonClickEvent");
        Checks.notNull(onComplete, "ButtonClickEvent");
        getButtonHandler(guild).addButton(id, null, onTrigger, onComplete);
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton onEvent(@Nonnull Guild guild, @Nonnull User allowedUser, @Nonnull Consumer<ButtonInteractionEvent> onTrigger, @Nonnull Consumer<ButtonInteractionEvent> onComplete) {
        Checks.notNull(id, "ID");
        Checks.notNull(guild, "Guild");
        Checks.notNull(allowedUser, "User");
        Checks.notNull(onTrigger, "ButtonClickEvent");
        Checks.notNull(onComplete, "ButtonClickEvent");
        getButtonHandler(guild).addButton(id, Collections.singletonList(allowedUser), onTrigger, onComplete);
        return this;
    }

    @Nonnull
    @CheckReturnValue
    public BetterButton onEvent(@Nonnull Guild guild, @Nonnull List<User> allowedUsers, @Nonnull Consumer<ButtonInteractionEvent> onTrigger, @Nonnull Consumer<ButtonInteractionEvent> onComplete) {
        Checks.notNull(id, "ID");
        Checks.notNull(guild, "Guild");
        Checks.notNull(allowedUsers, "Users");
        Checks.notNull(onTrigger, "ButtonClickEvent");
        Checks.notNull(onComplete, "ButtonClickEvent");
        getButtonHandler(guild).addButton(id, allowedUsers, onTrigger, onComplete);
        return this;
    }

    @Nonnull
    public Button build() {
        return Button.of(style, id, label, emoji);
    }

    private ButtonHandler getButtonHandler(@Nonnull Guild guild) {
        return CommandHandler.getButtonHandler(guild);
    }
}

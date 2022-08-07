package dev.JustRed23.grandfather.utils.btn;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class ButtonHandler {

    private final HashMap<String, Pair<Consumer<ButtonInteractionEvent>, Consumer<ButtonInteractionEvent>>> buttons;
    private final HashMap<String, List<User>> users;

    public ButtonHandler() {
        this.buttons = new HashMap<>();
        this.users = new HashMap<>();
    }

    protected synchronized void addButton(@NotNull String buttonId, @Nullable List<User> allowedUsers, @NotNull Consumer<ButtonInteractionEvent> onTrigger, @NotNull Consumer<ButtonInteractionEvent> onComplete) {
        if (buttons.containsKey(buttonId) || users.containsKey(buttonId))
            return;

        users.put(buttonId, allowedUsers == null ? Collections.emptyList() : allowedUsers);
        buttons.put(buttonId, Pair.of(onTrigger, onComplete));
    }

    public void handle(ButtonInteractionEvent event) {
        if (canHandle(event.getComponentId())) {
            //get onTrigger and onComplete from component id
            //get allowed users from component id
            //if allowed users is empty -> fire on trigger and on complete + remove the button listener
            //else if allowed users contains current user -> fire on trigger -> remove user from list -> if list is now empty fire on complete + remove the button listener
            //else ignore button click

            String buttonId = event.getComponentId();
            Consumer<ButtonInteractionEvent> onTrigger = buttons.get(buttonId).getLeft();
            Consumer<ButtonInteractionEvent> onComplete = buttons.get(buttonId).getRight();

            List<User> allowedUsers = new ArrayList<>(users.get(buttonId));
            User currentUser = event.getUser();

            if (allowedUsers.isEmpty()) {
                onTrigger.accept(event);
                onComplete.accept(event);
                removeButton(buttonId);
            } else {
                if (allowedUsers.contains(currentUser)) {
                    onTrigger.accept(event);

                    allowedUsers.remove(currentUser);
                    users.replace(buttonId, allowedUsers);

                    if (allowedUsers.isEmpty()) {
                        onComplete.accept(event);
                        removeButton(buttonId);
                    }
                } else event.deferEdit().queue();
            }
        }
    }

    private void removeButton(String buttonId) {
        buttons.remove(buttonId);
        users.remove(buttonId);
    }

    private boolean canHandle(String buttonId) {
        return buttons.containsKey(buttonId) && users.containsKey(buttonId);
    }
}

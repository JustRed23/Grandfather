package dev.JustRed23.grandfather.utils.btn;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.*;
import java.util.function.Consumer;

public class ButtonHandler {

    private final HashMap<Long, Map<String, BetterButton>> buttons;

    public ButtonHandler() {
        this.buttons = new HashMap<>();
    }

    protected synchronized void addButton(long msgID, BetterButton button) {
        String buttonId = button.id;
        Map<String, BetterButton> msgButtons = buttons.get(msgID);

        if (msgButtons != null && msgButtons.containsKey(buttonId)) {
            System.err.println("Button with ID " + buttonId + " already exists!");
            return;
        }

        if (msgButtons == null)
            msgButtons = new HashMap<>();

        msgButtons.put(buttonId, button);
        buttons.put(msgID, msgButtons);
    }

    public void handle(ButtonInteractionEvent event) {
        long msgID = event.getMessageIdLong();
        if (canHandle(msgID, event.getComponentId())) {
            //get onTrigger and onComplete from component id
            //get allowed users from component id
            //if allowed users is empty -> fire on trigger and on complete + remove the button listener
            //else if allowed users contains current user -> fire on trigger -> remove user from list -> if list is now empty fire on complete + remove the button listener
            //else ignore button click

            String buttonId = event.getComponentId();

            BetterButton button = buttons.get(msgID).get(buttonId);

            Consumer<ButtonInteractionEvent> onTrigger = button.onTrigger;
            Consumer<ButtonInteractionEvent> onComplete = button.onComplete;

            List<User> allowedUsers = Lists.newArrayList(button.allowedUsers);
            User currentUser = event.getUser();

            if (allowedUsers.isEmpty()) {
                onTrigger.accept(event);
                onComplete.accept(event);

                if (button.invalidateAfterUse)
                    removeButton(msgID, buttonId);
            } else {
                if (allowedUsers.contains(currentUser)) {
                    onTrigger.accept(event);

                    if (button.invalidateAfterUse) {
                        allowedUsers.remove(currentUser);
                        button.allowedUsers = allowedUsers;
                    }

                    if (allowedUsers.isEmpty() && button.invalidateAfterUse) {
                        onComplete.accept(event);
                        removeButton(msgID, buttonId);
                    }
                } else event.deferEdit().queue();
            }
        }
    }

    public void handleRemove(long msgID) {
        Map<String, BetterButton> msg = buttons.get(msgID);
        if (msg != null)
            buttons.remove(msgID);
    }

    public void removeButton(Long msgID, String buttonId) {
        Map<String, BetterButton> msg = buttons.get(msgID);
        if (msg != null) {
            msg.remove(buttonId);
            buttons.put(msgID, msg);
        }
    }

    private boolean canHandle(Long msgID, String buttonId) {
        Map<String, BetterButton> msg = buttons.get(msgID);
        if (msg == null)
            return false;

        return msg.containsKey(buttonId);
    }
}

package main.java.de.voidtech.alison.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class ButtonConsumer {

    public static final String TRUE_EMOTE = "\u2705";
    public static final String FALSE_EMOTE = "\u274C";

    private final Message message;
    private final ButtonClickEvent button;

    public ButtonConsumer(ButtonClickEvent button, Message message) {
        this.message = message;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.message.editMessageComponents().queue();
        }
    }

    public Message getMessage() {
        return this.message;
    }

    public ButtonClickEvent getButton() {
        return this.button;
    }

    public boolean userSaidYes() {
        return this.button.getComponentId().equals("YES");
    }
}

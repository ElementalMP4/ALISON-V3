package main.java.de.voidtech.alison.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class ButtonConsumer {

    public static final String TRUE_EMOTE = "\u2705";
    public static final String FALSE_EMOTE = "\u274C";

    private final ButtonInteractionEvent button;

    private Message message;
    private InteractionHook hook;

    public ButtonConsumer(ButtonInteractionEvent button, Message message) {
        this.message = message;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.message.editMessageComponents().queue();
        }
    }

    public ButtonConsumer(ButtonInteractionEvent button, InteractionHook hook) {
        this.hook = hook;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.hook.editOriginalComponents().queue();
        }
    }

    public void editResponse(String newContent) {
        if (message == null) {
            hook.editOriginal(newContent).queue();
        } else {
            message.editMessage(newContent).queue();
        }
    }

    public boolean userSaidYes() {
        return this.button.getComponentId().equals("YES");
    }
}

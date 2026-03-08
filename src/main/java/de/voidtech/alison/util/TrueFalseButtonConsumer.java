package main.java.de.voidtech.alison.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class TrueFalseButtonConsumer {

    public static final String TRUE_EMOTE = "\u2705";
    public static final String FALSE_EMOTE = "\u274C";

    private final ButtonInteractionEvent button;

    private Message message;
    private InteractionHook hook;

    public TrueFalseButtonConsumer(ButtonInteractionEvent button, Message message) {
        this.message = message;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.message.editMessageComponents().queue();
        }
    }

    public TrueFalseButtonConsumer(ButtonInteractionEvent button, InteractionHook hook) {
        this.hook = hook;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.hook.editOriginalComponents().queue();
        }
    }

    public void editResponse(MessageEmbed newContent) {
        if (message == null) {
            hook.editOriginalEmbeds(newContent).queue();
        } else {
            message.editMessageEmbeds(newContent).queue();
        }
    }

    public boolean userSaidYes() {
        return this.button.getComponentId().equals("YES");
    }
}

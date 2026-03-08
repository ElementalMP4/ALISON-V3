package main.java.de.voidtech.alison.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class NicknameButtonConsumer {

    public static final String YES = "YES";
    public static final String NO = "NO";
    public static final String RECYCLE = "RECYCLE";

    private final ButtonInteractionEvent button;

    private final Message message;
    private final InteractionHook hook;

    public NicknameButtonConsumer(ButtonInteractionEvent button, Message message) {
        this.hook = null;
        this.message = message;
        this.button = button;
        if (!this.button.isAcknowledged())  {
            this.button.deferEdit().queue();
            this.message.editMessageComponents().queue();
        }
    }

    public NicknameButtonConsumer(ButtonInteractionEvent button, InteractionHook hook) {
        this.message = null;
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

    public String getUserOption() {
        return this.button.getComponentId();
    }

    public Message getMessage() {
        return message;
    }

    public InteractionHook getHook() {
        return hook;
    }
}

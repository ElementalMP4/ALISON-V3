package main.java.de.voidtech.alison.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

import java.util.List;

public class PageButtonConsumer {

    private final ButtonInteractionEvent button;
    private final Message message;
    private final InteractionHook hook;

    private int page;

    public PageButtonConsumer(ButtonInteractionEvent button, Message message, int page) {
        this.button = button;
        this.message = message;
        this.hook = null;
        this.page = page;
        button.deferEdit().queue();
    }

    public PageButtonConsumer(ButtonInteractionEvent button, InteractionHook hook, int page) {
        this.button = button;
        this.hook = hook;
        this.message = null;
        this.page = page;
        button.deferEdit().queue();
    }

    public boolean isNext() {
        return button.getComponentId().startsWith("PAGE_NEXT");
    }

    public boolean isPrev() {
        return button.getComponentId().startsWith("PAGE_PREV");
    }

    public int getPage() {
        return page;
    }

    public void setPage(int newPage) {
        this.page = newPage;
    }

    public void edit(MessageEmbed embed, List<ActionComponent> buttons) {
        if (message != null) {
            message.editMessageEmbeds(embed)
                    .setActionRow(buttons)
                    .queue();
        } else {
            hook.editOriginalEmbeds(embed)
                    .setActionRow(buttons)
                    .queue();
        }
    }
}

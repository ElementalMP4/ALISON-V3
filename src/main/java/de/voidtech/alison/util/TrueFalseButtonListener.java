package main.java.de.voidtech.alison.util;

import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TrueFalseButtonListener {

    private List<ActionComponent> createTrueFalseButtons() {
        List<ActionComponent> components = new ArrayList<>();
        components.add(Button.secondary("YES", TrueFalseButtonConsumer.TRUE_EMOTE));
        components.add(Button.secondary("NO", TrueFalseButtonConsumer.FALSE_EMOTE));
        return components;
    }

    public TrueFalseButtonListener(CommandContext context, EventWaiter waiter, MessageEmbed question, Consumer<TrueFalseButtonConsumer> result) {
        if (context.isSlashCommand()) {
            InteractionHook hook = context.getEvent().replyEmbeds(question).setActionRow(createTrueFalseButtons()).mentionRepliedUser(false).complete();
            waiter.waitForEvent(ButtonInteractionEvent.class,
                    e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getHook().getId().equals(hook.getId()),
                    e -> result.accept(new TrueFalseButtonConsumer(e, hook)), 30, TimeUnit.SECONDS,
                    () -> hook.editOriginalComponents().queue());
        } else {
            Message msg = context.getMessage().replyEmbeds(question).setActionRow(createTrueFalseButtons()).mentionRepliedUser(false).complete();
            waiter.waitForEvent(ButtonInteractionEvent.class,
                    e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getMessage().getId().equals(msg.getId()),
                    e -> result.accept(new TrueFalseButtonConsumer(e, msg)), 30, TimeUnit.SECONDS,
                    () -> msg.editMessageComponents().queue());
        }
    }
}

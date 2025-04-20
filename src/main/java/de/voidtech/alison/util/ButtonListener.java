package main.java.de.voidtech.alison.util;

import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ButtonListener {

    private List<ActionComponent> createTrueFalseButtons() {
        List<ActionComponent> components = new ArrayList<>();
        components.add(Button.secondary("YES", ButtonConsumer.TRUE_EMOTE));
        components.add(Button.secondary("NO", ButtonConsumer.FALSE_EMOTE));
        return components;
    }

    public ButtonListener(CommandContext context, EventWaiter waiter, String question, Consumer<ButtonConsumer> result) {
        Message msg = context.getMessage().reply(question).setActionRow(createTrueFalseButtons()).mentionRepliedUser(false).complete();
        waiter.waitForEvent(ButtonInteractionEvent.class,
                e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getMessage().getId().equals(msg.getId()),
                e -> result.accept(new ButtonConsumer(e, msg)), 30, TimeUnit.SECONDS,
                () -> msg.editMessageComponents().queue());
    }
}

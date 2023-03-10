package main.java.de.voidtech.alison.util;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.alison.commands.CommandContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ButtonListener {

    private List<Component> createTrueFalseButtons() {
        List<Component> components = new ArrayList<>();
        components.add(Button.secondary("YES", ButtonConsumer.TRUE_EMOTE));
        components.add(Button.secondary("NO", ButtonConsumer.FALSE_EMOTE));
        return components;
    }

    public ButtonListener(CommandContext context, EventWaiter waiter, String question, Consumer<ButtonConsumer> result) {
        Message msg = context.getMessage().reply(question).setActionRow(createTrueFalseButtons()).mentionRepliedUser(false).complete();
        waiter.waitForEvent(ButtonClickEvent.class,
                e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getMessage().getId().equals(msg.getId()),
                e -> result.accept(new ButtonConsumer(e, msg)), 30, TimeUnit.SECONDS,
                () -> msg.editMessageComponents().queue());
    }
}

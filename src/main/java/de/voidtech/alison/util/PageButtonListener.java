package main.java.de.voidtech.alison.util;

import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class PageButtonListener {

    public PageButtonListener(
            CommandContext context,
            EventWaiter waiter,
            MessageEmbed initialEmbed,
            BiConsumer<PageButtonConsumer, Integer> onPageChange
    ) {
        int startPage = 0;

        List<ActionComponent> buttons = createButtons(startPage, true, true);

        if (context.isSlashCommand()) {
            InteractionHook hook = context.getEvent()
                    .replyEmbeds(initialEmbed)
                    .setActionRow(buttons)
                    .mentionRepliedUser(false)
                    .complete();

            waiter.waitForEvent(
                    ButtonInteractionEvent.class,
                    e -> e.getUser().getId().equals(context.getAuthor().getId())
                            && e.getHook().getId().equals(hook.getId()),
                    e -> handle(e, hook, startPage, onPageChange),
                    60, TimeUnit.SECONDS,
                    () -> hook.editOriginalComponents().queue()
            );
        } else {
            Message msg = context.getMessage()
                    .replyEmbeds(initialEmbed)
                    .setActionRow(buttons)
                    .mentionRepliedUser(false)
                    .complete();

            waiter.waitForEvent(
                    ButtonInteractionEvent.class,
                    e -> e.getUser().getId().equals(context.getAuthor().getId())
                            && e.getMessage().getId().equals(msg.getId()),
                    e -> handle(e, msg, startPage, onPageChange),
                    60, TimeUnit.SECONDS,
                    () -> msg.editMessageComponents().queue()
            );
        }
    }

    private void handle(
            ButtonInteractionEvent event,
            Object target,
            int currentPage,
            BiConsumer<PageButtonConsumer, Integer> callback
    ) {
        int page = currentPage;

        if (event.getComponentId().startsWith("PAGE_NEXT")) page++;
        if (event.getComponentId().startsWith("PAGE_PREV")) page--;

        page = Math.max(0, page);

        PageButtonConsumer consumer =
                target instanceof Message
                        ? new PageButtonConsumer(event, (Message) target, page)
                        : new PageButtonConsumer(event, (InteractionHook) target, page);

        callback.accept(consumer, page);
    }

    public static List<ActionComponent> createButtons(int page, boolean hasPrev, boolean hasNext) {
        return List.of(
                Button.primary("PAGE_PREV:" + page, "⬅ Prev").withDisabled(!hasPrev),
                Button.primary("PAGE_NEXT:" + page, "Next ➡").withDisabled(!hasNext)
        );
    }
}

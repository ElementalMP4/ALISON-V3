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
        if (context.isSlashCommand()) {
            InteractionHook hook = context.getEvent()
                    .replyEmbeds(initialEmbed)
                    .setActionRow(createButtons(0, false, true))
                    .mentionRepliedUser(false)
                    .complete();

            wait(context, waiter, hook, 0, onPageChange);
        } else {
            Message msg = context.getMessage()
                    .replyEmbeds(initialEmbed)
                    .setActionRow(createButtons(0, false, true))
                    .mentionRepliedUser(false)
                    .complete();

            wait(context, waiter, msg, 0, onPageChange);
        }
    }

    private void wait(
            CommandContext context,
            EventWaiter waiter,
            Object target,
            int page,
            BiConsumer<PageButtonConsumer, Integer> callback
    ) {
        waiter.waitForEvent(
                ButtonInteractionEvent.class,
                e -> e.getUser().getId().equals(context.getAuthor().getId())
                        && matchesTarget(e, target),
                e -> {
                    int newPage = page;

                    if (e.getComponentId().startsWith("PAGE_NEXT")) newPage++;
                    if (e.getComponentId().startsWith("PAGE_PREV")) newPage--;

                    newPage = Math.max(0, newPage);

                    PageButtonConsumer consumer =
                            target instanceof Message
                                    ? new PageButtonConsumer(e, (Message) target, newPage)
                                    : new PageButtonConsumer(e, (InteractionHook) target, newPage);

                    callback.accept(consumer, newPage);
                    wait(context, waiter, target, newPage, callback);
                },
                60, TimeUnit.SECONDS,
                () -> disableButtons(target)
        );
    }

    private boolean matchesTarget(ButtonInteractionEvent e, Object target) {
        if (target instanceof Message msg) {
            return e.getMessage().getId().equals(msg.getId());
        }
        InteractionHook hook = (InteractionHook) target;
        return e.getHook().getId().equals(hook.getId());
    }

    private void disableButtons(Object target) {
        if (target instanceof Message msg) {
            msg.editMessageComponents().queue();
        } else {
            ((InteractionHook) target).editOriginalComponents().queue();
        }
    }

    public static List<ActionComponent> createButtons(int page, boolean hasPrev, boolean hasNext) {
        return List.of(
                Button.primary("PAGE_PREV:" + page, "Previous").withDisabled(!hasPrev),
                Button.primary("PAGE_NEXT:" + page, "Next").withDisabled(!hasNext)
        );
    }
}

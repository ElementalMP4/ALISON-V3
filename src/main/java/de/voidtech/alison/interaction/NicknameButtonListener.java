package main.java.de.voidtech.alison.interaction;

import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.listeners.EventWaiter;
import main.java.de.voidtech.alison.util.Embeds;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NicknameButtonListener {

    private static final String TRUE_EMOTE = "✅";
    private static final String FALSE_EMOTE = "❌";
    private static final String RECYCLE_EMOTE = "♻️";

    private List<ActionComponent> createNicknameButtons() {
        List<ActionComponent> components = new ArrayList<>();
        components.add(Button.secondary(NicknameButtonConsumer.YES, TRUE_EMOTE));
        components.add(Button.secondary(NicknameButtonConsumer.NO, FALSE_EMOTE));
        components.add(Button.secondary(NicknameButtonConsumer.RECYCLE, RECYCLE_EMOTE));
        return components;
    }

    public NicknameButtonListener(CommandContext context, EventWaiter waiter, MessageEmbed question, InteractionHolder holder, Consumer<NicknameButtonConsumer> result) {
        if (context.isSlashCommand()) {
            handleSlashCommand(context, waiter, question, holder, result);
        } else {
            handleMessage(context, waiter, question, holder, result);
        }
    }

    private void handleSlashCommand(CommandContext context, EventWaiter waiter, MessageEmbed question, InteractionHolder holder, Consumer<NicknameButtonConsumer> result) {
        InteractionHook hook;
        if (holder == null) {
             hook = context.getEvent().replyEmbeds(question).setActionRow(createNicknameButtons()).mentionRepliedUser(false).complete();
        } else {
            holder.getHook().editOriginalEmbeds(question).setActionRow(createNicknameButtons()).complete();
            hook = holder.getHook();
        }

        waiter.waitForEvent(ButtonInteractionEvent.class,
                e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getHook().getId().equals(hook.getId()),
                e -> result.accept(new NicknameButtonConsumer(e, hook)), 30, TimeUnit.SECONDS,
                () -> {
                    hook.editOriginalComponents().queue();
                    hook.editOriginalEmbeds(Embeds.TimedOutEmbed).queue();
                });
    }

    private void handleMessage(CommandContext context, EventWaiter waiter, MessageEmbed question, InteractionHolder holder, Consumer<NicknameButtonConsumer> result) {
        Message msg;
        if (holder == null) {
            msg = context.getMessage().replyEmbeds(question).setActionRow(createNicknameButtons()).mentionRepliedUser(false).complete();
        } else {
            holder.getMessage().editMessageEmbeds(question).setActionRow(createNicknameButtons()).complete();
            msg = holder.getMessage();
        }
        waiter.waitForEvent(ButtonInteractionEvent.class,
                e -> e.getUser().getId().equals(context.getAuthor().getId()) && e.getMessage().getId().equals(msg.getId()),
                e -> result.accept(new NicknameButtonConsumer(e, msg)), 30, TimeUnit.SECONDS,
                () -> {
                    msg.editMessageComponents().queue();
                    msg.editMessageEmbeds(Embeds.TimedOutEmbed).queue();
                });
    }
}

package main.java.de.voidtech.alison.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class CommandContext {
    private final User author;
    private final Member member;
    private final Guild guild;
    private final JDA jda;

    private Message message;
    private SlashCommandInteractionEvent event;
    private List<String> args = List.of();

    public CommandContext(SlashCommandInteractionEvent event) {
        this.author = event.getUser();
        this.member = event.getMember();
        this.guild = event.getGuild();
        this.jda = event.getJDA();
        this.event = event;
    }

    private void assertMessageCommand() {
        if (message == null) {
            throw new IllegalStateException("Message was null when accessed");
        }
    }

    private void assertSlashCommand() {
        if (event == null) {
            throw new IllegalStateException("Slash command event was null when accessed");
        }
    }

    public CommandContext(Message message, List<String> args) {
        this.message = message;
        this.guild = !message.getChannel().getType().equals(ChannelType.TEXT) ? null : message.getGuild();
        this.author = message.getAuthor();
        this.member = message.getMember();
        this.jda = message.getJDA();
        this.args = args;
    }

    public void reply(String content) {
        if (this.isSlashCommand()) {
            this.event.reply(content).mentionRepliedUser(false).queue();
        } else {
            this.message.reply(content).mentionRepliedUser(false).queue();
        }
    }

    public void reply(MessageEmbed embed) {
        if (this.isSlashCommand()) {
            this.event.replyEmbeds(embed).mentionRepliedUser(false).queue();
        } else {
            this.message.replyEmbeds(embed).mentionRepliedUser(false).queue();
        }
    }

    public void replyAndThen(MessageEmbed embed, Consumer<Message> consumer) {
        assertMessageCommand();
        this.message.replyEmbeds(embed).mentionRepliedUser(false).queue(consumer);
    }

    public void replyWithFile(byte[] attachment, String attachmentName, MessageEmbed embed) {
        FileUpload upload = FileUpload.fromData(attachment, attachmentName);

        if (this.isSlashCommand()) {
            this.event.replyEmbeds(embed).mentionRepliedUser(false).addFiles(upload).queue();
        } else {
            this.message.replyEmbeds(embed).mentionRepliedUser(false).addFiles(upload).queue();
        }
    }

    public void replyErrorEmbed(String message) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":no_entry_sign: " + message)
                .build();
        reply(embed);
    }

    public User getAuthor() {
        return this.author;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public Member getMember() {
        return this.member;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public Message getMessage() {
        assertMessageCommand();
        return this.message;
    }

    public List<String> getArgs() {
        assertMessageCommand();
        return this.args;
    }

    public SlashCommandInteractionEvent getEvent() {
        assertSlashCommand();
        return this.event;
    }

    public boolean isSlashCommand() {
        return message == null;
    }

    public void replyInteractionAndThen(MessageEmbed embed, Consumer<InteractionHook> consumer) {
        assertSlashCommand();
        event.replyEmbeds(embed).mentionRepliedUser(false).queue(consumer);
    }

}
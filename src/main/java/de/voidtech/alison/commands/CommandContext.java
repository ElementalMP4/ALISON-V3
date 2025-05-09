package main.java.de.voidtech.alison.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.util.function.Consumer;

public class CommandContext {
    private final User author;
    private final Member member;
    private final Guild guild;
    private final JDA jda;
    private final Message message;

    public CommandContext(Message message) {
        this.message = message;
        this.guild = !message.getChannel().getType().equals(ChannelType.TEXT) ? null : message.getGuild();
        this.author = message.getAuthor();
        this.member = message.getMember();
        this.jda = message.getJDA();
    }

    public void reply(String content) {
        this.message.reply(content).mentionRepliedUser(false).queue();
    }

    public void reply(MessageEmbed embed) {
        this.message.replyEmbeds(embed).mentionRepliedUser(false).queue();
    }

    public void replyAndThen(String content, Consumer<Message> consumer) {
        this.message.reply(content).mentionRepliedUser(false).queue(consumer);
    }

    public void replyAndThen(MessageEmbed embed, Consumer<Message> consumer) {
        this.message.replyEmbeds(embed).mentionRepliedUser(false).queue(consumer);
    }

    public void replyWithFile(byte[] attachment, String attachmentName, MessageEmbed embed) {
        FileUpload upload = FileUpload.fromData(attachment, attachmentName);
        this.message.replyEmbeds(embed).mentionRepliedUser(false).addFiles(upload).queue();
    }

    public void replyErrorEmbed(String message) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":no_entry_sign: " + message)
                .build();
        reply(embed);
    }

    public void replySuccessEmbed(String message) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":white_check_mark: - " + message)
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
        return this.message;
    }
}
package main.java.de.voidtech.alison.commands.information;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import main.java.de.voidtech.alison.commands.SlashCommandOptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

@Command
public class PingCommand extends AbstractCommand{

	@Override
	public void execute(CommandContext context) {
		long time = System.currentTimeMillis();
		if (context.isSlashCommand()) {
			MessageEmbed beforePingHasBeenProcessedEmbed = new EmbedBuilder()
					.setAuthor("Ping?")
					.setColor(Color.RED)
					.build();
			context.replyInteractionAndThen(beforePingHasBeenProcessedEmbed, hook -> {
				MessageEmbed pingEmbed = new EmbedBuilder()
						.setAuthor("Pong!")
						.setColor(Color.GREEN)
						.setDescription(String.format("Latency: %sms\nGateway Latency: %sms",
								(System.currentTimeMillis() - time),
								context.getEvent().getJDA().getGatewayPing()))
						.build();
				hook.editOriginalEmbeds(pingEmbed).queue();
			});
		} else {
			MessageEmbed beforePingHasBeenProcessedEmbed = new EmbedBuilder()
					.setAuthor("Ping?")
					.setColor(Color.RED)
					.build();

			context.replyAndThen(beforePingHasBeenProcessedEmbed, response -> {
				MessageEmbed pingEmbed = new EmbedBuilder()
						.setAuthor("Pong!")
						.setColor(Color.GREEN)
						.setDescription(String.format("Latency: %sms\nGateway Latency: %sms",
								(System.currentTimeMillis() - time),
								context.getMessage().getJDA().getGatewayPing()))
						.build();
				response.editMessageEmbeds(pingEmbed).queue();
			});
		}
	}

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public String getUsage() {
		return "ping";
	}

	@Override
	public String getDescription() {
		return "Check ALISON's response time";
	}

	@Override
	public String getShorthand() {
		return "p";
	}

	@Override
	public boolean isDmCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFORMATION;
	}

	@Override
	public SlashCommandOptions getSlashCommandOptions() {
		return new SlashCommandOptions();
	}

}
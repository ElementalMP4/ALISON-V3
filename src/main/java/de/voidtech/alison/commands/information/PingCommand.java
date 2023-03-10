package main.java.de.voidtech.alison.commands.information;

import main.java.de.voidtech.alison.annotations.Command;
import main.java.de.voidtech.alison.commands.AbstractCommand;
import main.java.de.voidtech.alison.commands.CommandCategory;
import main.java.de.voidtech.alison.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;

@Command
public class PingCommand extends AbstractCommand{

	@Override
	public void execute(CommandContext context, List<String> args) {
		long time = System.currentTimeMillis();
			
		MessageEmbed beforePingHasBeenProcessedEmbed = new EmbedBuilder()
				.setAuthor("Ping?")
				.setColor(Color.RED)
				.build();
		
		context.replyAndThen(beforePingHasBeenProcessedEmbed, response -> {
			MessageEmbed pingEmbed = new EmbedBuilder()//
				.setAuthor("Pong!")
				.setColor(Color.GREEN)
				.setDescription(String.format("Latency: %sms\nGateway Latency: %sms",
						(System.currentTimeMillis() - time),
						context.getMessage().getJDA().getGatewayPing()))
				.build();
			response.editMessageEmbeds(pingEmbed).queue();
		});
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
		return "Allows you to see Alison's current response time and Discord API latency";
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

}
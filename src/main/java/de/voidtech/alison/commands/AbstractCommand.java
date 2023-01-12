package main.java.de.voidtech.alison.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.alison.service.ThreadManager;
import net.dv8tion.jda.api.entities.Message;

public abstract class AbstractCommand {
	@Autowired
	private ThreadManager threadManager;
	
	public void run(final Message message, final List<String> args) {
		Runnable webhookThreadRunnable = () -> execute(message, args);
		threadManager.getThreadByName("T-Command").execute(webhookThreadRunnable);   
	}
	
    public abstract void execute(final Message message, final List<String> args);
    public abstract String getName();
    public abstract String getShortName();
    public abstract String getUsage();
    public abstract String getDescription();
    public abstract boolean isHidden();
}
package main.java.de.voidtech.alison.listeners;

import main.java.de.voidtech.alison.service.CommandService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListener implements EventListener {

    @Autowired
    private CommandService commandService;

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent message = (MessageReceivedEvent) event;
            if (message.isWebhookMessage()) return;
            commandService.handleMessage(message.getMessage());
        }
    }
}
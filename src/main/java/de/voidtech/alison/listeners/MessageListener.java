package main.java.de.voidtech.alison.listeners;

import main.java.de.voidtech.alison.annotations.Listener;
import main.java.de.voidtech.alison.service.MessageService;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.springframework.beans.factory.annotation.Autowired;

@Listener
public class MessageListener implements EventListener {

    @Autowired
    private MessageService commandService;

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent message = (MessageReceivedEvent) event;
            if (message.isWebhookMessage()) return;
            commandService.handleMessage(message.getMessage());
        }
    }
}
package main.java.de.voidtech.alison.listeners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ReadyListener implements EventListener
{
    private static final Logger LOGGER = Logger.getLogger(ReadyListener.class.getName());;
    
    public void onEvent(final GenericEvent event) {
        if (event instanceof ReadyEvent) {
            final String clientName = ((ReadyEvent)event).getJDA().getSelfUser().getAsTag();
            LOGGER.log(Level.INFO, "Alison logged in as " + clientName);
        }
    }
}
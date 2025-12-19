package main.java.de.voidtech.alison.listeners;

import main.java.de.voidtech.alison.annotations.Listener;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.logging.Level;
import java.util.logging.Logger;

@Listener
public class ReadyListener implements EventListener
{
    private static final Logger LOGGER = Logger.getLogger(ReadyListener.class.getName());;
    
    public void onEvent(final GenericEvent event) {
        if (event instanceof ReadyEvent) {
            final String clientName = event.getJDA().getSelfUser().getName();
            LOGGER.log(Level.INFO, "Alison logged in as " + clientName);
        }
    }
}
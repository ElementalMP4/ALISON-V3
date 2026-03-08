package main.java.de.voidtech.alison.interaction;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class InteractionHolder {

    private final Message message;
    private final InteractionHook hook;

    public InteractionHolder(Message message) {
        this.message = message;
        this.hook = null;
    }

    public InteractionHolder(InteractionHook hook) {
        this.hook = hook;
        this.message = null;
    }

    public boolean isSlashCommand() {
        return message == null;
    }

    public InteractionHook getHook() {
        return hook;
    }

    public Message getMessage() {
        return message;
    }
}
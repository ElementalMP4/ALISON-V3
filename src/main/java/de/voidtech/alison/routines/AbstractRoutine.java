package main.java.de.voidtech.alison.routines;

import main.java.de.voidtech.alison.service.ThreadManager;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRoutine {

    @Autowired
    private ThreadManager threadManager;

    public void run(Message message) {
        Runnable routineThreadRunnable = () -> executeInternal(message);
        threadManager.getThreadByName("T-Routine").execute(routineThreadRunnable);
    }

    public abstract void executeInternal(Message message);

    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean isDmCapable();
}
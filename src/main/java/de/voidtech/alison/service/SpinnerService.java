package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.Spinner;
import main.java.de.voidtech.alison.listeners.MessageListener;
import net.dv8tion.jda.api.entities.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SpinnerService {

    @Autowired
    private SessionFactory sessionFactory;

    private List<Spinner> spinnerCache;

    public static final Logger LOGGER = Logger.getLogger(MessageListener.class.getSimpleName());

    @SuppressWarnings("unchecked")
    @EventListener(ApplicationReadyEvent.class)
    void reloadCache() {
        try (Session session = sessionFactory.openSession()) {
            final List<Spinner> list = (List<Spinner>) session
                    .createQuery("FROM Spinner WHERE isStillSpinning = true")
                    .list();
            this.spinnerCache = list;
            LOGGER.log(Level.INFO, "Loaded " + list.size() + " spinners into cache");
        }
    }

    private Spinner getSpinnerFromCache(String channelID) {
        return spinnerCache.stream().filter(s -> s.getChannelID().equals(channelID)).findFirst().orElse(null);
    }

    public boolean spinnerExistsInChannel(String channelID) {
        return getSpinnerFromCache(channelID) != null;
    }

    public void createSpinner(Message message) {
        Spinner spinner = new Spinner(message.getChannel().getId(), message.getAuthor().getId());
        spinnerCache.add(spinner);
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(spinner);
            session.getTransaction().commit();
        }
    }

    public Spinner endSpinnage(Message message) {
        Spinner spinner = getSpinnerFromCache(message.getChannel().getId());
        spinner.finishSpinner(message.getAuthor().getId());
        spinnerCache.remove(spinner);
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(spinner);
            session.getTransaction().commit();
        }
        return spinner;
    }
}
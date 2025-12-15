package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.listeners.MessageListener;
import main.java.de.voidtech.alison.persistence.entity.Spinner;
import main.java.de.voidtech.alison.persistence.repository.SpinnerRepository;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class SpinnerService {

    @Autowired
    private SpinnerRepository repository;

    private List<Spinner> spinnerCache;

    public static final Logger LOGGER = Logger.getLogger(MessageListener.class.getSimpleName());
    
    @Autowired
    private SpinnerRepository spinnerRepository;

    @EventListener(ApplicationReadyEvent.class)
    void reloadCache() {
        this.spinnerCache = repository.getSpinningSpinners();
        LOGGER.log(Level.INFO, "Loaded " + this.spinnerCache.size() + " spinners into cache");

        TimerTask task = new TimerTask() {
            public void run() {
                for (Spinner spinner : spinnerCache) {
                    if (!spinner.isStillSpinning()) return;
                    spinner.updateDuration();
                    spinnerRepository.save(spinner);
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 5000);
    }

    private Spinner getSpinnerFromCache(String channelID) {
        return spinnerCache.stream().filter(s -> s.getChannelID().equals(channelID)).findFirst().orElse(null);
    }

    public boolean spinnerExistsInChannel(String channelID) {
        return getSpinnerFromCache(channelID) != null;
    }

    public void createSpinner(Message message) {
        Spinner spinner = new Spinner(message.getGuild().getId(), message.getChannel().getId(), message.getAuthor().getId());
        spinnerCache.add(spinner);
        repository.save(spinner);
    }

    public Spinner endSpinnage(Message message) {
        Spinner spinner = getSpinnerFromCache(message.getChannel().getId());
        spinner.finishSpinner(message.getAuthor().getId());
        spinnerCache.remove(spinner);
        repository.save(spinner);
        return spinner;
    }

    public List<Spinner> getServerLeaderboard(String serverId) {
        return spinnerRepository.getSpinnerLeaderboardForServer(serverId, PageRequest.of(0, 10));
    }
}
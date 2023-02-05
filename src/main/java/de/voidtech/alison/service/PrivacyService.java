package main.java.de.voidtech.alison.service;

import main.java.de.voidtech.alison.entities.IgnoredChannel;
import main.java.de.voidtech.alison.entities.IgnoredUser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivacyService {

    @Autowired
    private SessionFactory sessionFactory;

    public void optOut(String id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(new IgnoredUser(id));
            session.getTransaction().commit();
        }
    }

    public void optIn(String id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM IgnoredUser WHERE userID = :userID").setParameter("userID", id).executeUpdate();
            session.getTransaction().commit();
        }
    }

    public boolean userHasOptedOut(String id) {
        try (Session session = sessionFactory.openSession()) {
            final IgnoredUser user = (IgnoredUser) session.createQuery("FROM IgnoredUser WHERE userID = :userID")
                    .setParameter("userID", id)
                    .uniqueResult();
            return user != null;
        }
    }

    public boolean channelIsIgnored(String channelId, String guildId) {
        try (Session session = sessionFactory.openSession()) {
            final IgnoredChannel channel = (IgnoredChannel) session
                    .createQuery("FROM IgnoredChannel WHERE channelID = :channelID AND guildID = :guildID")
                    .setParameter("channelID", channelId)
                    .setParameter("guildID", guildId)
                    .uniqueResult();
            return channel != null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getIgnoredChannelsForServer(String guildID) {
        try (Session session = sessionFactory.openSession()) {
            final List<IgnoredChannel> list = (List<IgnoredChannel>) session
                    .createQuery("FROM IgnoredChannel WHERE guildID = :guildID")
                    .setParameter("guildID", guildID)
                    .list();
            return list.stream().map(IgnoredChannel::getChannel).collect(Collectors.toList());
        }
    }

    public void ignoreChannel(String channelID, String guildID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(new IgnoredChannel(channelID, guildID));
            session.getTransaction().commit();
        }
    }

    public void unignoreChannel(String channelID, String guildID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM IgnoredChannel WHERE channelID = :channelID AND guildID = :guildID")
                    .setParameter("channelID", channelID)
                    .setParameter("guildID", guildID)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }
}
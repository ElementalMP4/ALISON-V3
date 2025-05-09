package main.java.de.voidtech.alison.service;

import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class WebhookManager {

    @Autowired
    private ThreadManager threadManager;

    private static final Logger LOGGER = Logger.getLogger(WebhookManager.class.getName());

    public Webhook getOrCreateWebhook(TextChannel targetChannel, String webhookName, String selfID) {

        List<Webhook> webhooks = targetChannel.retrieveWebhooks().complete();
        for (Webhook webhook : webhooks) {
            if (webhook.getName().equals(webhookName) && Objects.requireNonNull(webhook.getOwnerAsUser()).getId().equals(selfID)) {
                return webhook;
            }
        }
        return targetChannel.createWebhook(webhookName).complete();
    }

    public void sendWebhookMessage(Webhook webhook, String content, String username, String avatarUrl) {
        sendWebhookMessage(webhook.getUrl(), content, username, avatarUrl);
    }

    public void sendWebhookMessage(String webhookUrl, String content, String username, String avatarUrl) {
        ExecutorService executor = threadManager.getThreadByName("T-Webhook");
        Runnable webhookRunnable = () -> {
            JSONObject webhookPayload = new JSONObject();
            webhookPayload.put("content", content);
            webhookPayload.put("username", username);
            webhookPayload.put("avatar_url", avatarUrl);
            webhookPayload.put("tts", false);
            try {
                URL url = new URL(webhookUrl);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "Alison");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                OutputStream stream = connection.getOutputStream();
                stream.write(webhookPayload.toString().getBytes());
                stream.flush();
                stream.close();

                connection.getInputStream().close();
                connection.disconnect();

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + ex.getMessage());
                ex.printStackTrace();
            }
        };
        executor.execute(webhookRunnable);
    }
}
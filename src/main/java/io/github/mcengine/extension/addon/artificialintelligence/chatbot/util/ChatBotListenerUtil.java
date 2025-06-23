package io.github.mcengine.extension.addon.artificialintelligence.chatbot.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Utility class for sending email messages using Gmail or Outlook SMTP.
 * The sender's email credentials and settings are read from the custom config.
 */
public class ChatBotListenerUtil {

    /**
     * Sends an email to the specified recipient.
     *
     * @param plugin        The plugin instance used for config access and logging.
     * @param msg           The message body to send.
     * @param receiverEmail The recipient's email address.
     */
    public static void sendDataToEmail(Plugin plugin, String msg, String receiverEmail) {
        FileConfiguration config = ChatBotConfigLoader.getCustomConfig(plugin);

        String senderEmail = config.getString("mail.email");
        String senderPassword = config.getString("mail.password");
        String mailType = config.getString("mail.type", "gmail").toLowerCase();

        if (senderEmail == null || senderPassword == null) {
            plugin.getLogger().warning("Missing email credentials in custom config.yml.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        switch (mailType) {
            case "outlook" -> props.put("mail.smtp.host", "smtp.office365.com");
            case "gmail" -> props.put("mail.smtp.host", "smtp.gmail.com");
            default -> {
                plugin.getLogger().warning("Unknown mail.type: " + mailType + ". Defaulting to Gmail.");
                props.put("mail.smtp.host", "smtp.gmail.com");
            }
        }

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
            message.setSubject("Player ChatBot Session Data");
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            plugin.getLogger().severe("Failed to send email: " + e.getMessage());
        }
    }
}

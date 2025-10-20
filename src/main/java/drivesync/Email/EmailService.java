package drivesync.Email;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private static String USERNAME;
    private static String PASSWORD;
    private static String HOST;
    private static String PORT;

    static {
        try (InputStream input = EmailService.class
                .getClassLoader()
                .getResourceAsStream("drivesync/NO-GITHUB/email.properties")) {

            if (input == null) {
                System.err.println("❌ Nem található az email.properties fájl (drivesync/NO-GITHUB/email.properties)");
            } else {
                Properties config = new Properties();
                config.load(input);
                USERNAME = config.getProperty("email.username");
                PASSWORD = config.getProperty("email.password");
                HOST = config.getProperty("email.host", "smtp.gmail.com");
                PORT = config.getProperty("email.port", "587");

                System.out.println("✅ Email konfiguráció betöltve: " + HOST);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendEmail(String to, String subject, String body) {
        if (USERNAME == null || PASSWORD == null) {
            System.err.println("❌ Hiányzik az email konfiguráció. Ellenőrizd az email.properties fájlt!");
            return false;
        }

        // SMTP beállítások
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        // Hitelesítés
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("📧 Email elküldve: " + to);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Hiba az email küldésénél: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

package drivesync.Email;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    // 1. GreenMail indítása SMTP módban (ez a kamu szerver)
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testuser", "testpass"));

    @BeforeEach
    void setUp() throws Exception {
        // 2. REFLECTION: Mivel a mezők privátok és statikusak, erőszakkal átírjuk őket,
        // hogy ne a properties fájlt, hanem a GreenMail szervert használják.
        setPrivateStaticField("HOST", "localhost");
        setPrivateStaticField("PORT", String.valueOf(greenMail.getSmtp().getPort())); // A GreenMail portja
        setPrivateStaticField("USERNAME", "testuser");
        setPrivateStaticField("PASSWORD", "testpass");
    }

    @Test
    void testSendEmailSuccess() {
        // --- 1. Arrange (Előkészítés) ---
        String to = "ugyfel@example.com";
        String subject = "Teszt Üzenet";
        String body = "Ez egy JUnit teszt üzenet.";

        // --- 2. Act (Végrehajtás) ---
        boolean result = EmailService.sendEmail(to, subject, body);

        // --- 3. Assert (Ellenőrzés) ---
        // a) Ellenőrizzük, hogy a metódus true-val tért vissza
        assertTrue(result, "Az email küldésnek sikeresnek kell lennie");

        // b) Ellenőrizzük, hogy a GreenMail szerver megkapta-e az emailt
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length, "Pontosan 1 emailnek kellett érkeznie");

        // c) Ellenőrizzük a tartalmat
        try {
            MimeMessage msg = receivedMessages[0];
            assertEquals(subject, msg.getSubject());
            assertEquals(to, msg.getRecipients(jakarta.mail.Message.RecipientType.TO)[0].toString());
            // A body ellenőrzése kicsit trükkösebb lehet a Mime típusok miatt,
            // de egyszerű szövegnél ez gyakran működik:
            String content = msg.getContent().toString().trim();
            assertTrue(content.contains(body));
        } catch (Exception e) {
            fail("Hiba az email tartalmának ellenőrzésekor: " + e.getMessage());
        }
    }

    @Test
    void testSendEmailFailConfiguration() throws Exception {
        // Teszteljük, mi van, ha nincs beállítva felhasználónév (szimulált hiba)
        setPrivateStaticField("USERNAME", null);

        boolean result = EmailService.sendEmail("test@test.com", "Subj", "Body");

        assertFalse(result, "Ha nincs konfiguráció, false-t kell visszaadnia");
    }

    // Segédmetódus a privát static mezők átírásához (Reflection)
    private void setPrivateStaticField(String fieldName, String value) throws Exception {
        Field field = EmailService.class.getDeclaredField(fieldName);
        field.setAccessible(true); // Privát mező hozzáférhetővé tétele
        field.set(null, value);    // Statikus mező beállítása (null az objektum helyett)
    }
}
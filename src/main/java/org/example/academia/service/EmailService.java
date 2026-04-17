package org.example.academia.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Servicio de envío de correos electrónicos.
 *
 * Usa SMTP de Gmail por defecto. Para usar otro proveedor,
 * cambia host/port y credenciales.
 */
public class EmailService {

    // ──── Configuración SMTP (Gmail) ────
    // Usa una "Contraseña de aplicación" de Google, NO tu contraseña normal.
    // https://myaccount.google.com/apppasswords
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USER = "amph7019@gmail.com";       // ← Cambia esto
    private static final String SMTP_PASSWORD = "exxg ddub baaa ppub";        // ← Cambia esto
    private static final String FROM_NAME = "Academia de Belleza";

    /**
     * Envía un correo electrónico simple (texto plano).
     *
     * @param destinatario dirección de correo del receptor
     * @param asunto       asunto del mensaje
     * @param cuerpo       contenido del mensaje
     * @throws MessagingException si falla el envío
     */
    public void enviarCorreo(String destinatario, String asunto, String cuerpo) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);
        message.setText(cuerpo);

        Transport.send(message);
    }
}



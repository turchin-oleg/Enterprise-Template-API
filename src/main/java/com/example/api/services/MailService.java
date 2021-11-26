package com.example.api.services;

import com.example.api.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Oleg Turchin
 */
@Service
@Log4j2
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.senderName:Enterprise Template App<noreply@example.com>}")
    private String senderName;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailResetPassword (String contextPath, String token, User user) {
        String url = contextPath + "/auth/changePassword?token=" + token;
        String subj;
        try {
            String htmlAttach;
            switch (user.getLang().name()) {
                case "RU":
                    htmlAttach =new String(Files.readAllBytes(Paths.get("templates/email_templates/reset_password_ru.html")));
                    subj = "Измените свой пароль для Enterprise Template App";
                    break;
                case "UA":
                    htmlAttach =new String(Files.readAllBytes(Paths.get("templates/email_templates/reset_password_ua.html")));
                    subj = "Змініть свій пароль для Enterprise Template App";
                    break;
                case "EN":
                default:
                    htmlAttach =new String(Files.readAllBytes(Paths.get("templates/email_templates/reset_password_en.html")));
                    subj = "Change your password of Enterprise Template App";
            }
            htmlAttach = htmlAttach.replace("{{fullName}}", user.getFullName())
                    .replace("{{linkResetPassword}}", url);
            sendEmailWithAttachment(user.getEmail(), subj , htmlAttach);
        } catch (IOException | MessagingException e) {
            log.error(e);
        }
    }

    private void sendEmailWithAttachment(String emailTo, String subj, String htmlAttach)
            throws IOException, MessagingException {
        MimeMessage msg = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setFrom(senderName);
        helper.setTo(emailTo);
        helper.setSubject(subj);
        helper.setText(htmlAttach, true);
        javaMailSender.send(msg);
    }
}

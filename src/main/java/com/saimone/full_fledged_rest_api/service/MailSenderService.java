package com.saimone.full_fledged_rest_api.service;

import com.saimone.full_fledged_rest_api.exception.UnsentMessageException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class MailSenderService {
    @Getter
    @Setter
    @Value("${spring.mail.username}")
    String sendFrom;

    private final JavaMailSender javaMailSender;

    @Async
    public void sendMail(String emailTo, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(sendFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        try {
            javaMailSender.send(mailMessage);
        } catch (MailException ex) {
            log.error("IN sendMail - The message was not sent to the email: {}", emailTo);
            throw new UnsentMessageException("There was an error when sending an email. Please try again a little later.");
        }
    }
}
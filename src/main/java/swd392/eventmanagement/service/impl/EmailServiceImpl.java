package swd392.eventmanagement.service.impl;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import swd392.eventmanagement.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("threadPoolTaskExecutor")
    @Override
    public void sendEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            logger.info("Sending HTML email to: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

}

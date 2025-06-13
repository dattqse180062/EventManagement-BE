package swd392.eventmanagement.service;

import java.util.Map;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String templateName, Map<String, Object> variables);

    void sendEmailWithQRCodeAttachment(String toEmail, String subject, String templateName,
            Map<String, Object> variables, String qrCodeBase64,
            String attachmentName);

}

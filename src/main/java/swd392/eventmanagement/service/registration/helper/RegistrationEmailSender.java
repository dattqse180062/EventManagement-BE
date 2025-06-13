package swd392.eventmanagement.service.registration.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import swd392.eventmanagement.enums.EventMode;
import swd392.eventmanagement.model.entity.Event;
import swd392.eventmanagement.model.entity.Registration;
import swd392.eventmanagement.model.entity.User;
import swd392.eventmanagement.service.EmailService;

@Component
@RequiredArgsConstructor
public class RegistrationEmailSender {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationEmailSender.class);

    private final EmailService emailService;
    private final QRCodeGenerator qrCodeService;

    /**
     * Sends a registration confirmation email with QR code to the user.
     * 
     * @param user         The user who registered
     * @param event        The event for which the user registered
     * @param registration The registration record
     */
    public void sendRegistrationConfirmationEmail(User user, Event event, Registration registration) {
        try {
            logger.info("Sending registration confirmation email to: {}", user.getEmail());

            Map<String, Object> variables = buildConfirmationEmailVariables(user, event, registration);

            // Registration details including QR code
            String checkinCode = registration.getCheckinCode();
            logger.info("Raw checkinCode from registration: {}", checkinCode);

            String qrCodeBase64 = null;
            if (checkinCode != null && !checkinCode.isEmpty()) {
                qrCodeBase64 = qrCodeService.generateQRCodeAsBase64(checkinCode, 200, 200);
                logger.info("Generated QR code base64 length: {}",
                        qrCodeBase64 != null ? qrCodeBase64.length() : "null");
                variables.put("hasQRCode", true);
                logger.info("Set hasQRCode=true - QR code will be sent as attachment");
            } else {
                logger.warn("No checkinCode found for registration ID: {}", registration.getId());
                variables.put("hasQRCode", false);
                logger.info("Set hasQRCode=false");
            }

            // Send the email with QR code attachment
            if (qrCodeBase64 != null) {
                emailService.sendEmailWithQRCodeAttachment(
                        user.getEmail(),
                        "Registration Confirmation: " + event.getName(),
                        "event-registered",
                        variables,
                        qrCodeBase64,
                        "event-checkin-qrcode.png");
            } else {
                emailService.sendEmail(
                        user.getEmail(),
                        "Registration Confirmation: " + event.getName(),
                        "event-registered",
                        variables);
            }

            logger.info("Registration confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send registration confirmation email: {}", e.getMessage(), e);
            // Don't rethrow the exception as email sending should not block the
            // registration process
        }
    }

    /**
     * Sends a registration cancellation email to the user.
     * 
     * @param user         The user who cancelled their registration
     * @param event        The event for which the registration was cancelled
     * @param registration The registration record
     */
    public void sendRegistrationCancellationEmail(User user, Event event, Registration registration) {
        try {
            logger.info("Sending registration cancellation email to: {}", user.getEmail());

            Map<String, Object> variables = buildCancellationEmailVariables(user, event, registration);

            emailService.sendEmail(
                    user.getEmail(),
                    "Cancellation: " + event.getName(),
                    "event-cancelled",
                    variables);

            logger.info("Registration cancellation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send registration cancellation email: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> buildConfirmationEmailVariables(User user, Event event, Registration registration) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", "Event Registration Confirmation");
        variables.put("greeting", "Hello " + user.getFullName() + ",");

        Map<String, Object> eventDetails = buildEventDetails(event, registration);
        variables.put("eventDetails", eventDetails);

        variables.put("message", "Your registration for the event has been confirmed. " +
                "Please keep this email as reference and bring the attached QR code with you to the event for check-in.");

        variables.put("buttonText", "View Event Details");
        variables.put("buttonUrl", "http://localhost:3000/events/" + event.getId());

        return variables;
    }

    private Map<String, Object> buildCancellationEmailVariables(User user, Event event, Registration registration) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", "Event Registration Cancelled");
        variables.put("greeting", "Hello " + user.getFullName() + ",");

        Map<String, String> eventDetails = new HashMap<>();
        eventDetails.put("name", event.getName());
        eventDetails.put("date", formatEventDateTime(event.getStartTime(), event.getEndTime()));

        eventDetails.put("cancellationTime",
                registration.getCanceledAt() != null
                        ? registration.getCanceledAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                        : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        eventDetails.put("registrationTime",
                registration.getCreatedAt() != null
                        ? registration.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                        : "N/A");

        variables.put("eventDetails", eventDetails);

        variables.put("message",
                "<p>Your registration for the event <strong>" + event.getName()
                        + "</strong> has been successfully cancelled.</p>" +
                        "<p class=\"important-note\">" +
                        "<strong>Please note that you will not be able to re-register for this specific event once your registration has been cancelled.</strong>"
                        +
                        "</p>" +
                        "<p>We regret to see you go, but we hope to welcome you at our other exciting events in the future.</p>");

        variables.put("buttonText", "Explore Other Events");
        variables.put("buttonUrl", "http://localhost:3000/events");
        variables.put("hasQRCode", false);
        variables.put("hasAttachment", false);

        return variables;
    }

    private Map<String, Object> buildEventDetails(Event event, Registration registration) {
        Map<String, Object> eventDetails = new HashMap<>();
        eventDetails.put("name", event.getName());
        eventDetails.put("date", formatEventDateTime(event.getStartTime(), event.getEndTime()));

        // Add registration time
        eventDetails.put("registrationTime",
                registration.getCreatedAt() != null
                        ? registration.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                        : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        // Add check-in time window if available
        if (event.getCheckinStart() != null && event.getCheckinEnd() != null) {
            eventDetails.put("checkinWindow", "Check-in available from " +
                    event.getCheckinStart().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) +
                    " to " +
                    event.getCheckinEnd().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        }

        // Handle physical location if applicable (OFFLINE or HYBRID mode)
        if (event.getMode() == EventMode.OFFLINE || event.getMode() == EventMode.HYBRID) {
            if (event.getLocation() != null) {
                eventDetails.put("location", buildFullAddress(event));
            } else {
                eventDetails.put("location", null);
            }
        } else {
            eventDetails.put("location", null);
        }

        // Handle online platform if applicable (ONLINE or HYBRID mode)
        if (event.getMode() == EventMode.ONLINE || event.getMode() == EventMode.HYBRID) {
            if (event.getPlatform() != null) {
                Map<String, String> platformDetails = new HashMap<>();
                platformDetails.put("name", event.getPlatform().getName());
                if (event.getPlatform().getUrl() != null && !event.getPlatform().getUrl().isEmpty()) {
                    platformDetails.put("url", event.getPlatform().getUrl());
                } else {
                    platformDetails.put("url", null);
                }
                eventDetails.put("platform", platformDetails);
            } else {
                eventDetails.put("platform", null);
            }
        } else {
            eventDetails.put("platform", null);
        }

        // Add event mode for display purposes
        eventDetails.put("mode", event.getMode().toString());
        eventDetails.put("description",
                event.getDescription() != null ? event.getDescription() : "No additional details provided.");

        return eventDetails;
    }

    private String buildFullAddress(Event event) {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(event.getLocation().getAddress());

        // Add ward if available
        if (event.getLocation().getWard() != null && !event.getLocation().getWard().isEmpty()) {
            fullAddress.append(", Ward ").append(event.getLocation().getWard());
        }

        // Add district if available
        if (event.getLocation().getDistrict() != null && !event.getLocation().getDistrict().isEmpty()) {
            fullAddress.append(", ").append(event.getLocation().getDistrict());
        }

        // Add city if available
        if (event.getLocation().getCity() != null && !event.getLocation().getCity().isEmpty()) {
            fullAddress.append(", ").append(event.getLocation().getCity());
        }

        return fullAddress.toString();
    }

    /**
     * Helper method to format event date and time in a human-readable format
     * 
     * @param startTime Event start time
     * @param endTime   Event end time
     * @return Formatted date and time string
     */
    private String formatEventDateTime(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        String date = startTime.format(dateFormatter);
        String startTimeStr = startTime.format(timeFormatter);
        String endTimeStr = endTime.format(timeFormatter);

        return date + " from " + startTimeStr + " to " + endTimeStr;
    }
}

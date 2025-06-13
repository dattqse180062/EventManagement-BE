package swd392.eventmanagement.service.registration.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class CheckinCodeEncrypter {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    @Value("${app.encryption.secret-key}")
    private String secretKey;

    /**
     * Create a valid AES key from the configured secret key
     * 
     * @return SecretKeySpec with proper key length (32 bytes for AES-256)
     */
    private SecretKeySpec createAESKey() {
        try {
            // Hash the secret key to ensure consistent 32-byte length for AES-256
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error creating AES key: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt email and event ID into a single encrypted string
     * Format: email|eventId
     * 
     * @param email   the email to encrypt
     * @param eventId the event ID to encrypt
     * @return encrypted string in Base64 format containing both email and eventId
     */
    public String encryptEmailAndEventId(String email, Long eventId) {
        try {
            if (email == null || eventId == null) {
                throw new IllegalArgumentException("Email and Event ID cannot be null");
            } // Combine email and eventId with a separator
            String combined = email + "|" + eventId.toString();

            SecretKeySpec keySpec = createAESKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting email and event ID: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt a combined encrypted string back to email and event ID
     * 
     * @param encryptedData the encrypted string in Base64 format
     * @return array where [0] is email and [1] is eventId as string
     */
    public String[] decryptEmailAndEventId(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                throw new IllegalArgumentException("Encrypted data cannot be null or empty");
            }

            SecretKeySpec keySpec = createAESKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            String decryptedString = new String(decrypted, StandardCharsets.UTF_8);

            // Split by separator
            String[] parts = decryptedString.split("\\|");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid encrypted data format");
            }

            return parts; // [0] = email, [1] = eventId
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting email and event ID: " + e.getMessage(), e);
        }
    }
}

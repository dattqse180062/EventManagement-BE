package swd392.eventmanagement.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleTokenVerifierService {
    Payload verifyGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException;
    boolean isAllowedDomain(String email);
} 
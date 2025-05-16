package swd392.eventmanagement.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import swd392.eventmanagement.service.GoogleTokenVerifierService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleTokenVerifierServiceImpl implements GoogleTokenVerifierService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifierServiceImpl.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${app.auth.allowed-domains}")
    private String allowedDomainsString;
    
    private List<String> allowedDomains;
    
    @Value("${app.auth.allowed-domains}")
    public void setAllowedDomains(String allowedDomainsString) {
        this.allowedDomains = Arrays.asList(allowedDomainsString.split(","));
        logger.info("Allowed domains configured: {}", this.allowedDomains);
    }

    @Override
    public Payload verifyGoogleIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        logger.info("Verifying Google ID token");
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance(); 
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            logger.info("Google ID token verified successfully");
            return idToken.getPayload();
        }
        
        logger.warn("Invalid Google ID token");
        return null;
    }
    
    @Override
    public boolean isAllowedDomain(String email) {
        if (email == null) {
            return false;
        }
        
        for (String domain : allowedDomains) {
            if (email.endsWith("@" + domain)) {
                return true;
            }
        }
        
        logger.warn("Email domain not allowed: {}", email);
        return false;
    }
} 
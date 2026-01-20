package br.com.sistema.springaigemini.configurations;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

@Component
public class GmailAuthSetup {
    
    @Value("${gmail.client-id}")
    private String clientId;
    
    @Value("${gmail.client-secret}")
    private String clientSecret;
    
    @Value("${gmail.redirect-uri}")
    private String redirectUri;
    
    private static final String APPLICATION_NAME = "Gmail API Client";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    
    public Gmail getGmailService() throws Exception {
        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var credential = getCredentials(transport);
        
        return new Gmail.Builder(transport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }
    
    private Credential getCredentials(com.google.api.client.http.HttpTransport transport) throws Exception {
        // Cria secretos a partir do application.properties
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
            .setInstalled(new GoogleClientSecrets.Details()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUris(Collections.singletonList(redirectUri)));
        
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport,
                JSON_FACTORY,
                clientSecrets,
                Collections.singletonList(GmailScopes.GMAIL_MODIFY))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
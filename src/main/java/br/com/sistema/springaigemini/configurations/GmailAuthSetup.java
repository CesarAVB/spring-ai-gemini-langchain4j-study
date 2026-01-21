package br.com.sistema.springaigemini.configurations;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import jakarta.annotation.PostConstruct;

@Component
public class GmailAuthSetup {

	@Value("${gmail.client-id}")
	private String clientId;

	@Value("${gmail.client-secret}")
	private String clientSecret;

	@Value("${gmail.redirect-uri}")
	private String redirectUri;

	@Value("${gmail.refresh-token}")
	private String refreshToken;

	private static final String APPLICATION_NAME = "Gmail API Client";
	private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private GoogleAuthorizationCodeFlow flow;
	private HttpTransport transport;

	@PostConstruct
	public void init() throws Exception {
		this.transport = GoogleNetHttpTransport.newTrustedTransport();

		GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
				.setWeb(new GoogleClientSecrets.Details().setClientId(clientId).setClientSecret(clientSecret)
						.setRedirectUris(Collections.singletonList(redirectUri)));

		this.flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, clientSecrets,
				Collections.singletonList(GmailScopes.GMAIL_MODIFY))
				.setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance()).setAccessType("offline").build();
	}

	public Gmail getGmailService() throws Exception {
		// 1. Tenta carregar a credencial da memória
		Credential credential = flow.loadCredential("user");

		// 2. Se for nula ou não tiver o Refresh Token configurado, injetamos
		// manualmente
		if (credential == null || credential.getRefreshToken() == null) {
			TokenResponse tokenResponse = new TokenResponse();
			tokenResponse.setRefreshToken(refreshToken);

			// Cria e armazena na memória (DataStore)
			credential = flow.createAndStoreCredential(tokenResponse, "user");
		}

		// 3. Força a renovação do Access Token para garantir que a credencial está
		// ativa
		// Isso evita que o SDK tente abrir o navegador se o token expirar
		try {
			if (credential.getAccessToken() == null
					|| (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() < 60)) {
				credential.refreshToken();
			}
		} catch (Exception e) {
			// Se falhar aqui, o Refresh Token pode estar inválido ou revogado
			throw new RuntimeException("Erro ao renovar token do Gmail. Verifique o Refresh Token. " + e.getMessage());
		}

		return new Gmail.Builder(transport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}
}
package br.com.sistema.springaigemini.configurations;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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

	@PostConstruct
	public void init() throws Exception {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

		// Configuramos como "Web" para aceitar URIs de domínios reais
		GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
				.setWeb(new GoogleClientSecrets.Details().setClientId(clientId).setClientSecret(clientSecret)
						.setRedirectUris(Collections.singletonList(redirectUri)));

		this.flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, clientSecrets,
				Collections.singletonList(GmailScopes.GMAIL_MODIFY))
				// Usamos memória, pois o Refresh Token será recriado sempre no startup
				.setDataStoreFactory(MemoryDataStoreFactory.getDefaultInstance()).setAccessType("offline").build();
	}

	public Gmail getGmailService() throws Exception {
		// Tenta carregar da memória (se o app já estiver rodando há algum tempo)
		Credential credential = flow.loadCredential("user");

		// Se a memória estiver vazia (app acabou de subir), cria a credencial usando o
		// Refresh Token
		if (credential == null) {
			credential = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
					.setJsonFactory(JSON_FACTORY).setClientSecrets(clientId, clientSecret).build()
					.setRefreshToken(refreshToken);

			// Força a renovação do Access Token usando o Refresh Token agora mesmo
			credential.refreshToken();

			// Opcional: Armazenar no flow para chamadas subsequentes não repetirem o
			// refresh
			flow.createAndStoreCredential(new TokenResponse().setRefreshToken(refreshToken), "user");
		}

		return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}
}
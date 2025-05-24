package com.fitbit.fitbit_backend.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.HttpHeaders

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class FitbitApiService {

    private final WebClient webClient
    private final OAuth2AuthorizedClientService authorizedClientService
    private final ClientRegistrationRepository clientRegistrationRepository

    FitbitApiService(
            WebClient.Builder webClientBuilder,
            OAuth2AuthorizedClientService authorizedClientService,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.webClient = webClientBuilder.build()
        this.authorizedClientService = authorizedClientService
        this.clientRegistrationRepository = clientRegistrationRepository
    }

    // This method retrieves the authorized client for the "fitbit" registration
    // using Spring Security's in-memory storage.
    // The 'principalName' here will be the Fitbit user's encodedId after successful OAuth.
    private OAuth2AuthorizedClient getAuthorizedClient(String principalName) {
        def clientRegistration = clientRegistrationRepository.findByRegistrationId("fitbit")
        authorizedClientService.loadAuthorizedClient(clientRegistration.registrationId, principalName)
    }

    // Fetches food log entries for a given date
    JsonNode getFoodLog(String principalName, LocalDate date) {
        def authorizedClient = getAuthorizedClient(principalName)
        if (authorizedClient == null) {
            throw new IllegalStateException("Fitbit authorization not found. Please authenticate with Fitbit first.")
        }
        def accessToken = authorizedClient.accessToken.tokenValue
        def fitbitUserId = authorizedClient.principalName // This is the Fitbit user's encodedId
        def formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        println "Fetching food log for user: ${fitbitUserId} on date: ${formattedDate}"

        webClient.get()
                .uri("https://api.fitbit.com/1/user/${fitbitUserId}/foods/log/date/${formattedDate}.json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .bodyToMono(JsonNode)
                .block() // Blocking for simplicity; in a real app, use reactive
    }
}
package com.fitbit.fitbit_backend.controller

import com.fitbit.fitbit_backend.service.ProteinCalculationService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import java.time.LocalDate

@RestController
@RequestMapping("/api/protein")
class ProteinController {

    private final ProteinCalculationService proteinCalculationService

    ProteinController(ProteinCalculationService proteinCalculationService) {
        this.proteinCalculationService = proteinCalculationService
    }

    @GetMapping("/daily-intake")
    ResponseEntity<Map<String, Object>> getDailyProteinIntake(
            @AuthenticationPrincipal OAuth2AuthenticationToken oauthToken, // Spring Security injects the authenticated user
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (oauthToken == null) {
            // This case ideally shouldn't happen if Spring Security's filter chain is set up correctly,
            // as unauthenticated requests should be redirected to OAuth login first.
            return new ResponseEntity<>([message: "User not authenticated with Fitbit. Please go to /authorize/fitbit first."], HttpStatus.UNAUTHORIZED)
        }

        // The principal name for Fitbit in Spring Security OAuth2 is the Fitbit user's encodedId.
        // This ID is part of the OAuth2AuthenticationToken after successful authentication.
        def fitbitUserId = oauthToken.principal.name
        def targetDate = date ?: LocalDate.now() // Default to today if no date is provided

        println "Request received for protein intake for user: ${fitbitUserId} on date: ${targetDate}"

        try {
            BigDecimal dailyProtein = proteinCalculationService.calculateDailyProtein(fitbitUserId, targetDate)

            def response = [
                    date: targetDate.toString(),
                    totalProteinIntakeGrams: dailyProtein,
                    fitbitUserId: fitbitUserId // For verification
            ]

            return new ResponseEntity<>(response, HttpStatus.OK)
        } catch (IllegalStateException ise) {
            println "Authentication error: ${ise.message}"
            return new ResponseEntity<>([message: ise.message], HttpStatus.UNAUTHORIZED)
        } catch (Exception e) {
            println "An unexpected error occurred: ${e.message}"
            return new ResponseEntity<>([message: "Failed to retrieve protein intake: ${e.message}"], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
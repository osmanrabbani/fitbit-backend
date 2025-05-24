package com.fitbit.fitbit_backend.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service

import java.time.LocalDate

@Service
class ProteinCalculationService {

    private final FitbitApiService fitbitApiService

    ProteinCalculationService(FitbitApiService fitbitApiService) {
        this.fitbitApiService = fitbitApiService
    }

    BigDecimal calculateDailyProtein(String principalName, LocalDate date) {
        BigDecimal totalProtein = BigDecimal.ZERO

        try {
            JsonNode foodLog = fitbitApiService.getFoodLog(principalName, date)

            if (foodLog?.foods?.isArray()) {
                foodLog.foods.each { foodEntry ->
                    def nutrients = foodEntry.nutritionalValues
                    // Check if 'protein' field exists and is a number
                    if (nutrients?.has("protein") && nutrients.get("protein").isNumber()) {
                        totalProtein += nutrients.get("protein").asBigDecimal()
                    }
                }
            } else {
                println "No 'foods' array found or it's empty in Fitbit response for date: $date"
            }
        } catch (IllegalStateException ise) {
            // Re-throw the state exception for the controller to handle auth failure
            throw ise
        } catch (Exception e) {
            println "Error while processing Fitbit food log for date $date: ${e.message}"
            // Return 0 or throw a specific exception if you want to indicate failure
            totalProtein = BigDecimal.ZERO
        }

        totalProtein
    }
}
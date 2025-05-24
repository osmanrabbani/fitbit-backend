package com.fitbit.fitbit_backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

@SpringBootApplication
@RestController
class FitbitBackendApplication {

	static void main(String[] args) {
		SpringApplication.run(FitbitBackendApplication, args)
	}
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http
				.authorizeHttpRequests { authorize ->
					authorize
							.requestMatchers("/callback").permitAll() // Allow Fitbit to redirect here
							.requestMatchers("/authorize/fitbit").permitAll() // Endpoint to start OAuth flow
							.anyRequest().authenticated() // All other requests require authentication
				}
				.oauth2Login { oauth2Login ->
					oauth2Login
							.defaultSuccessUrl("/api/protein/daily-intake", true) // Redirect here after successful login
							.failureUrl("/login-error") // Optional: A page for failed login
				}
				.csrf().disable() // Disable CSRF for simplicity in development (for APIs without session)
		// In a production app, consider session management or stateless tokens like JWT for APIs.
		return http.build()
	}

	// A simple endpoint to initiate the Fitbit OAuth flow
	@Bean
	Closure<Object> authRedirectController() {
		{ ->
			new RedirectView("/oauth2/authorization/fitbit")
		}
	}
}

package com.mrtovo.commun.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge de l'application : à injecter plutôt que d'appeler LocalDate.now(), pour pouvoir fixer la date dans les tests.
 */
@Configuration
public class HorlogeConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}

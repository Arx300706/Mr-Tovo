package com.mrtovo.commun.config;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Pages d'erreur : templates/erreurs/403, 404 et 500 (toute autre erreur serveur → 500).
 */
@Configuration
public class ErreursConfig {

	private static final Set<Integer> PAGES = Set.of(403, 404, 500);

	@Bean
	public ErrorViewResolver erreursViewResolver() {
		return (request, status, model) -> {
			int code = status.is5xxServerError() ? 500 : status.value();
			if (!PAGES.contains(code)) {
				return null;
			}
			return new ModelAndView("erreurs/" + code, Map.of("title", "Erreur " + code), HttpStatus.valueOf(status.value()));
		};
	}

}

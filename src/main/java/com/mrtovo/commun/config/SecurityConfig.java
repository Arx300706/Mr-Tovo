package com.mrtovo.commun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;

/**
 * Sécurité de l'application (BK0-04).
 * Les règles par URL suivent la section 4 du TODO ; les contrôles plus fins
 * (« son département », « ses candidatures »…) restent dans les services de chaque piste.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String CANDIDAT = Role.CANDIDAT.name();
	private static final String EMPLOYE = Role.EMPLOYE.name();
	private static final String MANAGER = Role.MANAGER.name();
	private static final String RH = Role.RH.name();
	private static final String FINANCE = Role.FINANCE.name();
	private static final String ADMIN = Role.ADMIN.name();

	/**
	 * API REST CRUD reprise d'Exemple ({@code /api/**}) : RH et admin, en HTTP Basic, sans session ni CSRF.
	 * Chaîne séparée pour ne pas changer le comportement des pages (redirection vers /connexion).
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, CompteDetailsService compteDetailsService)
			throws Exception {
		http
			.securityMatcher("/api/**")
			.userDetailsService(compteDetailsService)
			.authorizeHttpRequests(auth -> auth.anyRequest().hasAnyRole(RH, ADMIN))
			.httpBasic(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CompteDetailsService compteDetailsService)
			throws Exception {
		http
			.userDetailsService(compteDetailsService)
			.authorizeHttpRequests(auth -> auth
				// Public (sauf le dépôt de candidature, réservé aux candidats)
				.requestMatchers("/carrieres/*/candidater", "/carrieres/*/postuler").hasRole(CANDIDAT)
				.requestMatchers("/", "/connexion", "/inscription", "/carrieres", "/carrieres/**").permitAll()
				.requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error", "/erreurs/**").permitAll()
				// Commun
				.requestMatchers("/admin/**").hasRole(ADMIN)
				.requestMatchers("/espace-candidat", "/espace-candidat/**").hasRole(CANDIDAT)
				.requestMatchers("/tableau-de-bord").hasAnyRole(EMPLOYE, MANAGER, RH, FINANCE, ADMIN)
				// Bloc 1 : besoins, postes, départements
				.requestMatchers("/besoins", "/besoins/**").hasAnyRole(MANAGER, RH, FINANCE)
				.requestMatchers("/postes", "/postes/**", "/departements", "/departements/**")
					.hasAnyRole(MANAGER, RH, FINANCE, ADMIN)
				// Bloc 2 : recrutement
				.requestMatchers("/offres/*/candidatures", "/offres/*/comparaison").hasAnyRole(MANAGER, RH)
				.requestMatchers("/offres", "/offres/**").hasRole(RH)
				.requestMatchers("/candidatures/**", "/candidats/**", "/entretiens", "/entretiens/**")
					.hasAnyRole(MANAGER, RH)
				// Bloc 3 et 4 : embauche, employés, contrats, onboarding
				.requestMatchers("/propositions/**").hasRole(RH)
				.requestMatchers("/employes", "/employes/**", "/contrats/**")
					.hasAnyRole(EMPLOYE, MANAGER, RH, FINANCE)
				.requestMatchers("/onboardings/**", "/onboarding-taches/**").hasAnyRole(EMPLOYE, MANAGER, RH)
				.requestMatchers("/mes-taches").hasAnyRole(EMPLOYE, MANAGER, RH, FINANCE, ADMIN)
				// Tout le reste (documents, notifications…) : connecté
				.anyRequest().authenticated())
			.formLogin(form -> form
				.loginPage("/connexion")
				.loginProcessingUrl("/connexion")
				.usernameParameter("identifiant")
				.passwordParameter("motDePasse")
				.successHandler(redirectionSelonRole())
				.failureHandler(retourConnexionEnErreur())
				.permitAll())
			.logout(logout -> logout
				.logoutUrl("/deconnexion")
				.logoutSuccessUrl("/connexion?deconnecte")
				.permitAll());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Retour sur la page demandée avant la connexion (ex. « Candidater » sur une offre),
	 * sinon candidat → son espace, employés → tableau de bord.
	 */
	private AuthenticationSuccessHandler redirectionSelonRole() {
		RequestCache requestCache = new HttpSessionRequestCache();
		return (request, response, authentication) -> {
			SavedRequest pageDemandee = requestCache.getRequest(request, response);
			if (pageDemandee != null) {
				requestCache.removeRequest(request, response);
				response.sendRedirect(pageDemandee.getRedirectUrl());
				return;
			}
			boolean candidat = authentication.getPrincipal() instanceof UtilisateurConnecte u && u.estCandidat();
			response.sendRedirect(request.getContextPath() + (candidat ? "/espace-candidat" : "/tableau-de-bord"));
		};
	}

	/** Revient sur le bon onglet (employé / candidat) avec le message d'erreur. */
	private AuthenticationFailureHandler retourConnexionEnErreur() {
		return (request, response, exception) -> {
			String espace = CompteDetailsService.ESPACE_CANDIDAT.equals(request.getParameter(CompteDetailsService.PARAM_ESPACE))
				? CompteDetailsService.ESPACE_CANDIDAT
				: CompteDetailsService.ESPACE_EMPLOYE;
			response.sendRedirect(request.getContextPath() + "/connexion?erreur&espace=" + espace);
		};
	}

}

package com.mrtovo.commun.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

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

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CompteDetailsService compteDetailsService)
			throws Exception {
		http
			.userDetailsService(compteDetailsService)
			.authorizeHttpRequests(auth -> auth
				// Public
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

	/** Candidat → son espace, employés → tableau de bord. */
	private AuthenticationSuccessHandler redirectionSelonRole() {
		return (request, response, authentication) -> {
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

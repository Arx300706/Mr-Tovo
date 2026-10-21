package com.mrtovo.config;

import com.mrtovo.security.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/offres/**", "/connexion", "/css/**", "/js/**", "/error/**", "/error")
                        .permitAll()
                        .requestMatchers("/candidat/**").hasRole(Roles.CANDIDAT)
                        .requestMatchers("/manager/**").hasRole(Roles.MANAGER)
                        .requestMatchers("/rh/**").hasRole(Roles.RH)
                        .requestMatchers("/finance/**").hasRole(Roles.FINANCE)
                        .requestMatchers("/directeur/**").hasRole(Roles.DIRECTEUR)
                        .requestMatchers("/espace/**").hasRole(Roles.EMPLOYE)
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/connexion")
                        .loginProcessingUrl("/connexion")
                        .usernameParameter("identifiant")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/tableau-de-bord")
                        .failureUrl("/connexion?erreur")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/deconnexion")
                        .logoutSuccessUrl("/?deconnecte")
                        .permitAll())
                .securityContext(ctx -> ctx.securityContextRepository(securityContextRepository()));
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

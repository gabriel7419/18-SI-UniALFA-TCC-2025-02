package edu.unialfa.alberguepro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests((requests) -> requests
                    .requestMatchers("/webjars/**", "/login").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN") // Apenas usuários com role ADMIN podem acessar /admin/...
                    .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/login?logout") // Redireciona para a pág. de login com msg
                .permitAll()
            );

    return http.build();
}

@Bean
public UserDetailsService userDetailsService() {
    // Cria um usuário comum
    UserDetails user =
            User.withDefaultPasswordEncoder()
                    .username("user")
                    .password("password")
                    .roles("USER")
                    .build();

    // Cria um usuário administrador
    UserDetails admin =
            User.withDefaultPasswordEncoder()
                    .username("admin")
                    .password("admin123")
                    .roles("ADMIN")
                    .build();

    return new InMemoryUserDetailsManager(user, admin);
}
}
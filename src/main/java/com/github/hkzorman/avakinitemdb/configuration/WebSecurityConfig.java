package com.github.hkzorman.avakinitemdb.configuration;

import com.github.hkzorman.avakinitemdb.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Bean
    public CustomFilter customFilter(HttpSecurity http) throws Exception {
        return new CustomFilter(authenticationManager(http));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Arrays.asList("*"));
                    configuration.setAllowedMethods(Arrays.asList("*"));
                    configuration.setAllowedHeaders(Arrays.asList("*"));
                    return configuration;
                }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/users/save")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/items/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/proposals/**")).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling
                            .authenticationEntryPoint((request, response, authException) -> {
                                logger.info("Returning error response due to: " + authException.getMessage());
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                            });
                })
                .addFilterAfter(customFilter(http), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(UserRepository repository) throws IOException {

        logger.info("Loading users from database...");
        var users = repository.findAll();
        logger.info("Loaded " + users.size() + " users");

        var usersDetails = new ArrayList<UserDetails>(users.size());
        for (var user : users) {
            logger.info("Adding user: " + user.getUsername() + ". Is Admin: " + user.getAdmin());
            usersDetails.add(User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getAdmin() != null && user.getAdmin() == true ? "admin" : "")
                    .build());
        }

        return new InMemoryUserDetailsManager(usersDetails);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
}

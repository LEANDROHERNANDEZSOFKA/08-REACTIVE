package co.sofka.config;

import co.sofka.AuthenticationRequest;
import co.sofka.JwtAuthenticationManager;
import co.sofka.JwtService;
import co.sofka.adapters.MongoUserAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService(MongoUserAdapter mongoUserAdapter) {
        return email -> mongoUserAdapter.getUserByEmail(new AuthenticationRequest(email))
                .flatMap(userRequest -> {
                    if (userRequest == null) {
                        return Mono.error(new UsernameNotFoundException("User not found"));
                    }

                    return Mono.just(org.springframework.security.core.userdetails.User.builder()
                            .username(userRequest.getEmail())
                            .password(userRequest.getPassword())
                            .roles(userRequest.getRole().toString())
                            .build());
                });
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);
        return authManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(JwtService jwtService) {
        return new JwtAuthenticationManager(jwtService);
    }

}

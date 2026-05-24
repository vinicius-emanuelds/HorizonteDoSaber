package com.poo.siga.config;

import com.poo.siga.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /** Swagger habilitado via variável de ambiente — desligado por padrão */
    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Público — login, troca de senha obrigatória e infra
                    auth.requestMatchers("/auth/**").permitAll();
                    auth.requestMatchers("/login", "/trocar-senha").permitAll();

                    // Assets estáticos
                    auth.requestMatchers("/css/**", "/js/**", "/favicon.png", "/manifest.json",
                        "/images/**", "/fonts/**", "/webjars/**", "/error").permitAll();

                    // Actuator — apenas health básico é público; demais exigem ADMIN
                    auth.requestMatchers("/actuator/health").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");

                    // Swagger/OpenAPI — protegido quando habilitado, bloqueado quando desabilitado
                    if (swaggerEnabled) {
                        auth.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html")
                                .hasRole("ADMIN");
                    } else {
                        auth.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html")
                                .denyAll();
                    }

                    // === USUARIOS: Somente ADMIN ===
                    auth.requestMatchers("/api/usuarios/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/modelos-grade/**").hasRole("ADMIN");

                    // === NOTAS: ADMIN, COORDENADOR ou PROFESSOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/notas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR");
                    auth.requestMatchers(HttpMethod.PUT, "/api/notas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR");

                    // === FREQUENCIA: ADMIN, COORDENADOR ou PROFESSOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/frequencias/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR");
                    auth.requestMatchers(HttpMethod.PUT, "/api/frequencias/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR");

                    // === MATRICULAS: ADMIN, COORDENADOR ou OPERADOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/matriculas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/matriculas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");

                    // === ALUNOS CRUD: ADMIN, COORDENADOR ou OPERADOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/alunos/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");
                    auth.requestMatchers(HttpMethod.PUT, "/api/alunos/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/alunos/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/alunos/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");

                    // === TURMAS: ADMIN ou COORDENADOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PUT, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");

                    // === PROFESSORES e DISCIPLINAS: Somente ADMIN ===
                    auth.requestMatchers(HttpMethod.POST, "/api/professores/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/professores/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/professores/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/professores/**").hasRole("ADMIN");

                    auth.requestMatchers(HttpMethod.POST, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/disciplinas/**").hasRole("ADMIN");

                    // === ANO LETIVO: Somente ADMIN pode criar/alterar/encerrar ===
                    auth.requestMatchers(HttpMethod.POST, "/api/anos-letivos/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/anos-letivos/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/anos-letivos/**").hasRole("ADMIN");

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            if (req.getHeader("Authorization") == null) {
                                res.sendRedirect("/login");
                            } else {
                                res.setStatus(401);
                            }
                        }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

package com.poo.siga.config;

import com.poo.siga.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Público — login e infra
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Assets estáticos
                .requestMatchers("/css/**", "/js/**", "/favicon.png", "/manifest.json").permitAll()

                // Páginas do frontend — precisam de autenticação via JS
                .requestMatchers("/", "/aluno", "/professor", "/turma", "/disciplina",
                    "/nota", "/matricula", "/frequencia", "/usuario", "/turma-detalhe", "/relatorios").permitAll()

                // GETs de API liberados (frontend carrega dados via fetch)
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // === USUARIOS: Somente ADMIN ===
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // === NOTAS: ADMIN, COORDENADOR ou PROFESSOR ===
                .requestMatchers(HttpMethod.POST, "/api/notas/**").hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/notas/**").hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR")

                // === FREQUENCIA: ADMIN, COORDENADOR ou PROFESSOR ===
                .requestMatchers(HttpMethod.POST, "/api/frequencias/**").hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/frequencias/**").hasAnyRole("ADMIN", "COORDENADOR", "PROFESSOR")

                // === MATRICULAS: ADMIN, COORDENADOR ou OPERADOR ===
                .requestMatchers(HttpMethod.POST, "/api/matriculas/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/matriculas/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")

                // === ALUNOS CRUD: ADMIN, COORDENADOR ou OPERADOR ===
                .requestMatchers(HttpMethod.POST, "/api/alunos/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")
                .requestMatchers(HttpMethod.PUT, "/api/alunos/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/alunos/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/alunos/**").hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR")

                // === TURMAS: ADMIN ou COORDENADOR ===
                .requestMatchers(HttpMethod.POST, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.PUT, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR")

                // === PROFESSORES e DISCIPLINAS: Somente ADMIN ===
                .requestMatchers(HttpMethod.POST, "/api/professores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/professores/**").hasAnyRole("ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.PATCH, "/api/professores/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/professores/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/disciplinas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/disciplinas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/disciplinas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/disciplinas/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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

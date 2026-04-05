package com.example.demostracion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.example.demostracion.service.UsuarioDetailsService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UsuarioDetailsService userDetailsService;
    
    @Value("${BCrypt_STRENGTH:12}")
    private int bcryptStrength;

    public SecurityConfig(UsuarioDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * BCryptPasswordEncoder con fuerza configurable
     * Fuerza 12 = ~200ms (producción)
     * Fuerza 10 = ~100ms (desarrollo - más rápido)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    // 👇 Aquí decides a dónde va cada rol
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isGerente = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"));
                boolean isVendedor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));
            boolean isConductor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CONDUCTOR"));
                    

            if (isAdmin) {
                response.sendRedirect("/dashboard");
            } else if (isGerente) {
                response.sendRedirect("/gerente");
            } else if (isVendedor) {
                response.sendRedirect("/vendedor/panel");
            } else if (isConductor) {
                response.sendRedirect("/vendedor/panel");
            } else {
                response.sendRedirect("/"); // por defecto
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authProvider());

        // ✅ CSRF protección habilitada
        http.csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**", "/reset-passwords-debug/**", "/setup/**", "/email-test/**")  // API sin CSRF + DEBUG endpoints
        );

        // ✅ Headers de seguridad
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com; " +
                "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com data:; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self';"
            ))
            .frameOptions(frame -> frame.sameOrigin())
        );

        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/", "/index", "/oficina/**", "/api/financiamiento/**", "/css/**", "/images/**", "/js/**", "/uploads/**", "/login", "/registro", "/recuperar", "/restablecer", "/restablecer/**", "/error/**", "/imagenes/**", "/home-banners.json").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/setup/**", "/debug/**", "/usuarios-test/**", "/info/**", "/db-debug/**", "/auth-debug/**", "/email-test/**", "/inbox-debug/**", "/reset-passwords-debug/**").permitAll()

                // Portal cliente
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                
                .requestMatchers("/admin/promociones/**").hasRole("ADMIN")

                // Rutas específicas de ADMIN
                .requestMatchers("/dashboard/**", "/admin/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/usuarios/**", "/roles/**").hasAnyRole("ADMIN", "GERENTE")
                
                // Rutas específicas de GERENTE
                .requestMatchers("/gerente/**").hasRole("GERENTE")
                .requestMatchers("/clima/**").hasRole("GERENTE")
                
                // Rutas específicas de CONDUCTOR
                .requestMatchers("/conductor/**").hasRole("CONDUCTOR")

                // Rutas específicas de VENDEDOR
                .requestMatchers("/vendedor/**").hasAnyRole("VENDEDOR", "CONDUCTOR")
                
                // Rutas compartidas entre ADMIN y GERENTE
                .requestMatchers("/novedades/**").hasAnyRole("ADMIN", "GERENTE")
                
                // Inventario y vehículos - solo para roles autorizados
                .requestMatchers("/inventario/**").hasAnyRole("ADMIN", "GERENTE")
                .requestMatchers("/vehiculos/**").hasAnyRole("ADMIN", "GERENTE")
                
                // Email - solo ADMIN
                .requestMatchers("/email/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customSuccessHandler()) // 👈 aquí usamos el handler
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                // Máximo 1 sesión por usuario
                .maximumSessions(1)
                .expiredUrl("/login-expired")
            );

        return http.build();
    }

    /**
     * HttpSessionEventPublisher para manejar eventos de sesión expirada
     * Configura el timeout automáticamente desde application.properties
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

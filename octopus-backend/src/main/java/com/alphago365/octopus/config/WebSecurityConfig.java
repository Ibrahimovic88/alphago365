package com.alphago365.octopus.config;

import com.alphago365.octopus.security.JwtAuthenticationEntryPoint;
import com.alphago365.octopus.security.JwtRequestFilter;
import com.alphago365.octopus.security.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    static String[] allWhitelist = {
            // static resource whitelist
            "/",
            "/**/api/v2",
            "/login",
            "/error",
            "/favicon.ico",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.jpg",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js",

            // swagger whitelist
            "/**/v2/api-docs",
            "/**/v2/api-docs.yml",
            "/**/swagger-resources",
            "/**/swagger-resources/configuration/ui",
            "/**/swagger-resources/configuration/security",
            "/**/swagger-ui.html",
            "/**/webjars/**",

            // user management whitelist
            "/**/users/username-in-use",
            "/**/users/email-in-use",
            "/**/users/cellphone-in-use",
            "/**/users/sign-in",
            "/**/users/sign-up",
            "/**/users/sign-out",

            // match related whitelist
            "/**/matches/**",

            // odds related whitelist
            "/**/matches/**/odds/**",

            // test
            "/**/test/**"
    };

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example

        httpSecurity.csrf().disable()
                // don't authenticate this particular request
                .authorizeRequests()

                .antMatchers(allWhitelist)
                .permitAll()

                // all other requests need to be authenticated
                .anyRequest().authenticated()
                // make sure we use stateless session; session won't be used to store user's state.
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost")
                        .allowedOrigins("http://127.0.0.1")
                        .allowedOrigins("http://localhost:8080")
                        .allowedOrigins("http://127.0.0.1:8080")
                ;
            }
        };
    }
}
package fr.gouv.tac.analytics.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;

@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AnalyticsProperties analyticsProperties;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(STATELESS).and()
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable();

        http.oauth2ResourceServer()
                .jwt();

        http.authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .antMatchers("/status").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final var keySpec = Base64.getMimeDecoder()
                .decode(analyticsProperties.getRobertJwtAnalyticsPublicKey());
        final var publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keySpec));
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(RS256)
                .build();

        // By default NimbusJwtDecoder use the JwtTimeStampValidator with its default clock skew
        // (org.springframework.security.oauth2.jwt.JwtTimestampValidator.DEFAULT_MAX_CLOCK_SKEW)
        jwtDecoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));

        return jwtDecoder;
    }
}

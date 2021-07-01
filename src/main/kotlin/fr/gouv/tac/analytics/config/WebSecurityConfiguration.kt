package fr.gouv.tac.analytics.config

import lombok.RequiredArgsConstructor
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*

@EnableWebSecurity
@RequiredArgsConstructor
class WebSecurityConfiguration(val analyticsProperties: AnalyticsProperties) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
        http.oauth2ResourceServer()
            .jwt()
        http.authorizeRequests()
            .requestMatchers(EndpointRequest.toAnyEndpoint())
            .permitAll()
//            .antMatchers("/status").permitAll()
//            .anyRequest().authenticated()
    }

    @Bean
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun jwtDecoder(): JwtDecoder {
        val keySpec: ByteArray = Base64.getMimeDecoder()
            .decode(analyticsProperties.robertJwtAnalyticsPublicKey)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keySpec)) as RSAPublicKey
        return NimbusJwtDecoder.withPublicKey(publicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build()
    }
}
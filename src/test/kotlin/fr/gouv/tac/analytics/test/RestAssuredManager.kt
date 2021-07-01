package fr.gouv.tac.analytics.test

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class RestAssuredManager : TestExecutionListener {
    companion object {
        private val JWT_KEY_PAIR: KeyPair?
        fun defaultJwtClaims(): JWTClaimsSet.Builder {
            return JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plus(2, ChronoUnit.MINUTES)))
        }

        private fun givenBaseHeaders(): RequestSpecification {
            return RestAssured.given()
                .accept(ContentType.JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, Locale.US)
        }

        fun givenAuthenticated(): RequestSpecification {
            return givenBaseHeaders()
                .header(
                    AUTHORIZATION, "Bearer ${generateToken(defaultJwtClaims())}"
                )
        }

        fun givenJwt(claims: JWTClaimsSet.Builder): RequestSpecification {
            return givenBaseHeaders()
                .header(AUTHORIZATION, "Bearer ${generateToken(claims)}")
        }

        private fun generateToken(claims: JWTClaimsSet.Builder): String {
            val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
            val signedJWT = SignedJWT(header, claims.build())
            signedJWT.sign(RSASSASigner(JWT_KEY_PAIR!!.private))
            return signedJWT.serialize()
        }

        init {
            // generate a JWT key pair and export system property to configure the test application
            try {
                JWT_KEY_PAIR = KeyPairGenerator.getInstance("RSA")
                    .generateKeyPair()
                val jwtPublicKey = Base64.getEncoder()
                    .encodeToString(JWT_KEY_PAIR.public.encoded)
                System.setProperty(
                    "analytics.robert-jwt-analytics-public-key",
                    jwtPublicKey
                )
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        RestAssured.port = testContext.applicationContext
            .environment
            .getRequiredProperty("local.server.port", Int::class.java)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }
}
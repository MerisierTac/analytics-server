package fr.gouv.tac.analytics.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CLASS

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestExecutionListeners(
    listeners = [RestAssuredManager::class, KafkaManager::class, LogbackManager::class],
    mergeMode = MERGE_WITH_DEFAULTS
)
@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS)
annotation class IntegrationTest

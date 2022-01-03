package fr.gouv.tac.analytics

import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.ZonedDateTime.now
import java.time.temporal.ChronoUnit.SECONDS

class TemporalMatchersTest {

    private val now = now()

    @TestFactory
    fun returns_true_if_result_is_between_now_and_ten_seconds_ago(): Iterable<DynamicTest> {
        val tenSecondsAgo = now.minus(10, SECONDS)
        return List(30) { now.minusSeconds(it.toLong()) }
            .map { date ->
                if (date.isAfter(tenSecondsAgo).or(date.isEqual(tenSecondsAgo))
                    .and(date.isBefore(now).or(date.isEqual(now)))
                ) {
                    dynamicTest("$date matches because between $now and 10 seconds before") {
                        assertThat(date.toString(), isStringDateBetweenNowAndTenSecondsAgo())
                    }
                } else {
                    dynamicTest("$date does not match because more than 10 seconds before $now") {
                        assertThat(date.toString(), not(isStringDateBetweenNowAndTenSecondsAgo()))
                    }
                }
            }
    }
}

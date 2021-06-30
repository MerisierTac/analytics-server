package fr.gouv.tac.analytics.test

import fr.gouv.tac.analytics.controller.AnalyticsController
import org.exparity.hamcrest.date.ZonedDateTimeMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

object TemporalMatchers {
    /**
     * Hamcrest matcher to verify a string representation of a datetime is between now and 10 seconds ago.
     */
    fun isStringDateBetweenNowAndTenSecondsAgo(): Matcher<String> {
        return object: TypeSafeMatcher<String>() {
            override fun matchesSafely(value: String): Boolean {
                val actualDate = ZonedDateTime.parse(value)
                return isBetweenNowAndTenSecondsAgo().matches(actualDate)
            }

            override fun describeTo(description: Description) {
                isBetweenNowAndTenSecondsAgo().describeTo(description)
            }
        }
    }

    /**
     * Hamcrest matcher to verify a [ZonedDateTime] is between now and 10 seconds ago.
     */
    private fun isBetweenNowAndTenSecondsAgo(): Matcher<ZonedDateTime> = Matchers.allOf(
            ZonedDateTimeMatchers.sameOrAfter(ZonedDateTime.now().minusSeconds(10)),
            ZonedDateTimeMatchers.sameOrBefore(ZonedDateTime.now())
        )
}
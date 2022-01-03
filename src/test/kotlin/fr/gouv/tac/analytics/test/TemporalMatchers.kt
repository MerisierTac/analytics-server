package fr.gouv.tac.analytics.test

import org.exparity.hamcrest.date.ZonedDateTimeMatchers.sameOrAfter
import org.exparity.hamcrest.date.ZonedDateTimeMatchers.sameOrBefore
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

object TemporalMatchers {
    /**
     * Hamcrest matcher to verify a string representation of a datetime is between now and 10 seconds ago.
     */
    fun isStringDateBetweenNowAndTenSecondsAgo(): Matcher<String> {
        return object : TypeSafeMatcher<String>() {
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
    private fun isBetweenNowAndTenSecondsAgo(): Matcher<ZonedDateTime> {
        val now = now()
        return allOf(
            sameOrAfter(now.minusSeconds(10)),
            sameOrBefore(now)
        )
    }
}

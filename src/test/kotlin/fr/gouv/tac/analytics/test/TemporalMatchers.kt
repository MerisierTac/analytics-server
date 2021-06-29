package fr.gouv.tac.analytics.test

import org.exparity.hamcrest.date.ZonedDateTimeMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import java.time.ZonedDateTime

object TemporalMatchers {
    /**
     * Hamcrest matcher to verify a string representation of a datetime is between now and 10 seconds ago.
     */
    val isStringDateBetweenNowAndTenSecondsAgo: TypeSafeMatcher<String?>
        get() {
            val dateTimeBetweenNowAndOneSecondAgo = isBetweenNowAndTenSecondsAgo
            return object : TypeSafeMatcher<String?>() {
                override fun matchesSafely(value: String?): Boolean {
                    val actualDate = ZonedDateTime.parse(value)
                    return dateTimeBetweenNowAndOneSecondAgo.matches(actualDate)
                }

                override fun describeTo(description: Description) {
                    dateTimeBetweenNowAndOneSecondAgo.describeTo(description)
                }
            }
        }

    /**
     * Hamcrest matcher to verify a [ZonedDateTime] is between now and 10 seconds ago.
     */
    private val isBetweenNowAndTenSecondsAgo: Matcher<ZonedDateTime>
        get() = Matchers.allOf(
            ZonedDateTimeMatchers.sameOrAfter(ZonedDateTime.now().minusSeconds(10)),
            ZonedDateTimeMatchers.sameOrBefore(ZonedDateTime.now())
        )
}
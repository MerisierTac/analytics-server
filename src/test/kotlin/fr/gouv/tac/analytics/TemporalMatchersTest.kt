package fr.gouv.tac.analytics

import fr.gouv.tac.analytics.test.TemporalMatchers.isStringDateBetweenNowAndTenSecondsAgo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.ZonedDateTime

class TemporalMatchersTest {

    companion object {
        @JvmStatic
        fun timesNearNow() = listOf(
            Arguments.arguments({ ZonedDateTime.now() }),
            Arguments.arguments({ ZonedDateTime.now().minusSeconds(5) }),
            Arguments.arguments({ ZonedDateTime.now().minusSeconds(9) })
        )

        @JvmStatic
        fun timesMoreThanTenSecondsAgo() = listOf(
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(10) }),
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(15) }),
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(19) })
        )

        @JvmStatic
        fun timesInFuture() = listOf(
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(1) }),
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(5) }),
            Arguments.arguments({ ZonedDateTime.now().plusSeconds(9) })
        )
    }

    @ParameterizedTest
    @MethodSource("timesNearNow")
    fun can_verify_a_date_is_near_now_and_ten_seconds_ago(someTime: () -> ZonedDateTime) {
        assertThat(someTime().toString(), isStringDateBetweenNowAndTenSecondsAgo())
    }

    @ParameterizedTest
    @MethodSource("timesMoreThanTenSecondsAgo")
    fun can_detect_a_date_is_more_than_ten_seconds_ago(someTime: () -> ZonedDateTime) {
        assertThrows<AssertionError> {
            assertThat(someTime().toString(), isStringDateBetweenNowAndTenSecondsAgo())
        }
    }

    @ParameterizedTest
    @MethodSource("timesInFuture")
    fun can_detect_a_date_is_in_the_future(someTime: () -> ZonedDateTime) {
        assertThrows<AssertionError> {
            assertThat(someTime().toString(), isStringDateBetweenNowAndTenSecondsAgo())
        }
    }
}

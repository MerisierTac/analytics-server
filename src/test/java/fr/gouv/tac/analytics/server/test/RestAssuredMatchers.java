package fr.gouv.tac.analytics.server.test;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.AllOf;

import java.time.ZonedDateTime;

public class RestAssuredMatchers {

    /**
     * Hamcrest matcher to verify a string representation of a datetime is between now and 10 seconds ago.
     */
    public static Matcher<String> isStringDateBetweenNowAndTenSecondAgo() {
        final var dateTimeBetweenNowAndOneSecondAgo = new AllOf<>(isBetweenNowAndTenSecondAgo());
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(String value) {
                final var actualDate = ZonedDateTime.parse(value);
                return dateTimeBetweenNowAndOneSecondAgo.matches(actualDate);
            }

            @Override
            public void describeTo(Description description) {
                dateTimeBetweenNowAndOneSecondAgo.describeTo(description);
            }
        };
    }

    /**
     * Hamcrest matcher to verify a {@link ZonedDateTime} is between now and 10 seconds ago.
     */
    private static Matcher<ZonedDateTime> isBetweenNowAndTenSecondAgo() {
        return Matchers.allOf(
                ZonedDateTimeMatchers.sameOrAfter(ZonedDateTime.now().minusSeconds(10)),
                ZonedDateTimeMatchers.sameOrBefore(ZonedDateTime.now())
        );
    }
}

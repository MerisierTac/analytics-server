package fr.gouv.tac.analytics.test

import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions
import org.assertj.core.api.ListAssert
import org.slf4j.LoggerFactory
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener
import ch.qos.logback.classic.Logger as LogbackLogger

class LogbackManager : TestExecutionListener {

    companion object {
        private val LOG_EVENTS = ListAppender<ILoggingEvent>()

        fun assertThatInfoLogs(): ListAssert<String> = Assertions.assertThat(
            LOG_EVENTS.list
                .filter { it.level == INFO }
                .map { it.formattedMessage }
        )
    }

    override fun beforeTestMethod(testContext: TestContext) {
        val rootLogger = LoggerFactory.getLogger(ROOT_LOGGER_NAME) as LogbackLogger
        rootLogger.addAppender(LOG_EVENTS)
        LOG_EVENTS.start()
        LOG_EVENTS.list.clear()
    }
}

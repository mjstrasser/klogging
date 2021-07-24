package io.klogging.impl

import io.klogging.events.Level
import io.klogging.logEvent
import io.klogging.logger
import io.klogging.randomString
import io.klogging.savedEvents
import io.klogging.waitForDispatch
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Instant

class KtLoggerImplTest : DescribeSpec({
    describe("KtLoggerImpl implementation of KtLogger") {
        describe("logs any object") {
            it("logs a string in the message field") {
                val events = savedEvents()
                val message = randomString()
                KloggerImpl("KtLoggerImplTest").warn(message)
                waitForDispatch()

                events.size shouldBe 1
                events.first().message shouldBe message
            }
            it("logs a LogEvent object with the specified level") {
                val events = savedEvents()
                val event = logEvent()
                KloggerImpl("KtLoggerImplTest").warn(event)
                waitForDispatch()

                events.size shouldBe 1
                with(events.first()) {
                    timestamp shouldBe event.timestamp
                    host shouldBe event.host
                    logger shouldBe event.logger
                    level shouldBe Level.WARN
                    template shouldBe event.template
                    message shouldBe event.message
                    stackTrace shouldBe event.stackTrace
                    items shouldBe event.items
                }
            }
            it("logs a LogEvent object with stack trace from any exception") {
                val events = savedEvents()
                val event = logEvent()
                val exception = RuntimeException("Oh noes!")
                KloggerImpl("KtLoggerImplTest").error(exception, event)
                waitForDispatch()

                events.size shouldBe 1
                with(events.first()) {
                    timestamp shouldBe event.timestamp
                    host shouldBe event.host
                    logger shouldBe event.logger
                    level shouldBe Level.ERROR
                    template shouldBe event.template
                    message shouldBe event.message
                    stackTrace shouldBe exception.stackTraceToString()
                    items shouldBe event.items
                }
            }
            it("logs an exception with message and stack trace") {
                val events = savedEvents()
                val exception = RuntimeException("Some kind of problem")
                KloggerImpl("KtLoggerImplTest").warn(exception)
                waitForDispatch()

                events.size shouldBe 1
                events.first().message shouldBe exception.message
                events.first().stackTrace shouldNotBe null
            }
            it("logs the string representation of anything else in the message field") {
                val events = savedEvents()
                val event = Instant.now()
                logger("KtLoggerImplTest").info(event)
                waitForDispatch()

                events.size shouldBe 1
                events.first().message shouldBe event.toString()
            }
        }

        describe("optionally logs exception information") {
            it("does not include stack trace information if an exception is not provided") {
                val events = savedEvents()
                logger("KtLoggerImplTest").warn { "Possible trouble" }
                waitForDispatch()

                events.size shouldBe 1
                events.first().stackTrace shouldBe null
            }
            it("includes stack trace information if an exception is provided as well as other information") {
                val events = savedEvents()
                logger("KtLoggerImplTest").warn(RuntimeException("Oh noes!")) { "Big trouble!" }
                waitForDispatch()

                events.size shouldBe 1
                events.first().stackTrace shouldNotBe null
            }
        }

        describe("event construction function e()") {
            it("uses the template unchanged as message if there are no items") {
                val tmpl = randomString()
                with(KloggerImpl("KtLoggerImplTest").e(tmpl)) {
                    message shouldBe tmpl
                    template shouldBe tmpl
                }
            }
            it("uses message templating to complete the message") {
                val tmpl = "Hello {User}!"
                val item = randomString()
                with(KloggerImpl("KtLoggerImplTest").e(tmpl, item)) {
                    message shouldBe tmpl
                    template shouldBe tmpl
                    items shouldContain ("User" to item)
                }
            }
        }
    }
})
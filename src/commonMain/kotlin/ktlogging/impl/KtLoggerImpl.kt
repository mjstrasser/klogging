package ktlogging.impl

import ktlogging.KtLogger
import ktlogging.Logging
import ktlogging.context.LogContext
import ktlogging.events.Level
import ktlogging.events.LogEvent
import ktlogging.events.newId
import ktlogging.events.now
import ktlogging.template.templateItems
import kotlin.coroutines.coroutineContext

class KtLoggerImpl(
    override val name: String,
    private val minLevel: Level = Level.INFO,
) : KtLogger {

    override fun minLevel() = minLevel

    override suspend fun logMessage(level: Level, exception: Exception?, event: Any?) {
        val eventToLog = when (event) {
            is LogEvent -> event.copyWith(level, exception?.stackTraceToString())
            else -> {
                val (message, stackTrace) = when (event) {
                    is Exception -> (event.message ?: "Exception") to event.stackTraceToString()
                    else -> event.toString() to exception?.stackTraceToString()
                }
                LogEvent(
                    id = newId(),
                    timestamp = now(),
                    logger = name,
                    level = level,
                    message = message,
                    stackTrace = stackTrace,
                    items = contextItems()
                )
            }
        }

        Logging.sendEvent(eventToLog)
    }

    private suspend inline fun contextItems() =
        coroutineContext[LogContext]?.getAll() ?: mapOf()

    override suspend fun e(template: String, vararg values: Any?): LogEvent {
        val items = templateItems(template, *values).mapValues { e -> e.value.toString() }
        return LogEvent(
            id = newId(),
            timestamp = now(),
            logger = name,
            level = minLevel,
            template = template,
            message = template,
            stackTrace = null,
            items = items + contextItems(),
        )
    }
}
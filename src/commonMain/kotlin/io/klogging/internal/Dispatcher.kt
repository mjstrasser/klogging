/*

   Copyright 2021 Michael Strasser.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package io.klogging.internal

import io.klogging.Level
import io.klogging.events.LogEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Object that handles dispatching of [LogEvent]s to zero or more sinks. */
internal object Dispatcher {

    /**
     * Dispatch a [LogEvent] to selected targets.
     *
     * Each is dispatched in a separate coroutine.
     */
    public suspend fun dispatchEvent(logEvent: LogEvent): Unit = coroutineScope {
        sinksFor(logEvent.logger, logEvent.level)
            .forEach { sink ->
                launch {
                    debug("Dispatching event ${logEvent.id}")
                    sink.emitEvent(logEvent)
                }
            }
    }

    /**
     * Calculate the sinks for the specified logger and level.
     *
     * @param loggerName name of the logger
     * @param level level at which to emit logs
     *
     * @return the list of [Sink]s for this logger at this level, which may be empty
     */
    internal fun sinksFor(loggerName: String, level: Level): List<Sink> {
        val sinkNames = KloggingEngine.configs()
            .filter { it.nameMatch.matches(loggerName) }
            .flatMap { it.ranges }
            .filter { level in it }
            .flatMap { it.sinkNames }
            .distinct()
        return KloggingEngine.sinks()
            .filterKeys { it in sinkNames }
            .map { it.value }
    }
}

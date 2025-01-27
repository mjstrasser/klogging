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

package io.klogging.config

import io.klogging.Level
import io.klogging.Level.FATAL
import io.klogging.Level.TRACE

/**
 * Logging configuration with a logger name match and a list of level ranges that
 * maps to a list of sinks for each.
 */
public class LoggingConfig {
    /** Default matching is all logger names. */
    internal val matchAllLoggers: String = ".*"

    internal var nameMatch: Regex = Regex(matchAllLoggers)
    internal val ranges = mutableListOf<LevelRange>()

    /**
     * DSL function to specify that logger names should match from the specified base name.
     *
     * @param baseName match logger names from this base
     */
    @ConfigDsl
    public fun fromLoggerBase(baseName: String) {
        nameMatch = Regex("^$baseName.*")
    }

    /**
     * DSL function to specify that logger names should match this name exactly.
     *
     * @param exactName match this logger name exactly
     */
    @ConfigDsl
    public fun exactLogger(exactName: String) {
        nameMatch = Regex("^$exactName\$")
    }

    /**
     * DSL function to specify the minimum level from which to log.
     *
     * @param minLevel inclusive minimum level
     * @param configBlock configuration for this range of levels
     */
    @ConfigDsl
    public fun fromMinLevel(
        minLevel: Level,
        configBlock: LevelRange.() -> Unit
    ) {
        val range = LevelRange(minLevel, FATAL)
        range.apply(configBlock)
        if (range.sinkNames.isNotEmpty()) ranges.add(range)
    }

    /**
     * DSL function to specify a specific level at which to log.
     *
     * @param level exact level
     * @param configBlock configuration for this range of levels
     */
    @ConfigDsl
    public fun atLevel(level: Level, configBlock: LevelRange.() -> Unit) {
        val range = LevelRange(level, level)
        range.apply(configBlock)
        if (range.sinkNames.isNotEmpty()) ranges.add(range)
    }

    /**
     * DSL function to specify a sink where events for all logging levels should be sent.
     *
     * @param sinkName name of the sink
     */
    @ConfigDsl
    public fun toSink(sinkName: String) {
        val range = LevelRange(TRACE, FATAL)
        range.toSink(sinkName)
        if (range.sinkNames.isNotEmpty()) ranges.add(range)
    }
}

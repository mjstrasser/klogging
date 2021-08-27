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
import io.klogging.config.KloggingConfiguration
import io.klogging.config.LoggingConfig
import io.klogging.config.SinkConfiguration
import io.klogging.config.configLoadedFromFile
import io.klogging.events.LogEvent

/**
 * Object that is the centre of Klogging processing. 
 *
 * All static state should be managed here. 
 */
public object KloggingEngine {
    
    private val DEFAULT_CONFIG = KloggingConfiguration()

    private const val CURRENT_STATE = "CURRENT_STATE"
    private val currentState: MutableMap<String, KloggingConfiguration> =
        mutableMapOf(CURRENT_STATE to DEFAULT_CONFIG)

    /** Lazily loaded property that is only set when we have a Klogging state. */
    internal val configuration: KloggingConfiguration by lazy {
        debug("Lazy-loading current configuration")
        configLoadedFromFile?.let { setConfig(it) }
        currentConfig()
    }

    /** Sets a new value of the current config.  */
    internal fun setConfig(config: KloggingConfiguration) {
        // No synchronisation or locking yet.
        currentState[CURRENT_STATE] = config
    }

    internal fun appendConfig(config: KloggingConfiguration) {
        currentConfig().append(config)
    }

    private fun currentConfig() = currentState[CURRENT_STATE] ?: DEFAULT_CONFIG
    
    private val currentSinks: MutableMap<String, Sink> = mutableMapOf()
    private fun SinkConfiguration.toSender() =
        { e: LogEvent -> dispatcher(renderer(e)) }
    private fun setSinks(sinkConfigs: Map<String, SinkConfiguration>) {
        currentSinks.values.forEach { it.stop() }
        currentSinks.clear()
        currentSinks.putAll(
            sinkConfigs.map { (name, config) ->
                name to Sink(name, config.toSender())
            }
        )
    }

    // Functions returning the current state.
    internal fun minimumLevelOf(name: String): Level = currentConfig().minimumLevelOf(name)

    internal fun sinks(): Map<String, Sink> = currentSinks

    internal fun configs(): List<LoggingConfig> = currentConfig().configs

    internal fun kloggingMinLogLevel(): Level = currentConfig().kloggingMinLogLevel
}

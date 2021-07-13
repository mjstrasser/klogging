package ktlogging.clef

import ktlogging.events.LogEvent

expect fun LogEvent.toClef(): String

expect fun dispatchClef(clefEvent: String, server: String = "http://localhost:5341")
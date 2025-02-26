package com.t8rin.logger

import android.util.Log

data object Logger {

    inline fun <reified T> makeLog(
        tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
        level: Level = Level.Debug,
        dataBlock: () -> T
    ) {
        val data = dataBlock()
        val message = if (data is Throwable) {
            Log.getStackTraceString(data)
        } else data.toString()

        when (level) {
            is Level.Assert -> Log.println(level.priority, tag, message)
            Level.Debug -> Log.d(tag, message)
            Level.Error -> Log.e(tag, message)
            Level.Info -> Log.i(tag, message)
            Level.Verbose -> Log.v(tag, message)
            Level.Warn -> Log.w(tag, message)
        }
    }

    inline fun <reified T> makeLog(
        tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
        level: Level = Level.Debug,
        data: T
    ) = makeLog(tag = tag, level = level) { data }

    fun makeLog(
        level: Level = Level.Debug,
        separator: String = " - ",
        vararg data: Any
    ) {
        makeLog(level = level) { data.toList().joinToString(separator) { it.toString() } }
    }

    sealed interface Level {
        data class Assert(
            val priority: Int
        ) : Level

        data object Error : Level
        data object Warn : Level
        data object Info : Level
        data object Debug : Level
        data object Verbose : Level
    }

}

inline fun <reified T> T.makeLog(
    tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
    level: Logger.Level = Logger.Level.Debug,
    dataBlock: (T) -> Any? = { it }
): T = also {
    Logger.makeLog(
        tag = tag,
        level = level,
        dataBlock = { dataBlock(it) }
    )
}

inline infix fun <reified T> T.makeInfixLog(
    tag: String
): T = makeLog(tag) { this }
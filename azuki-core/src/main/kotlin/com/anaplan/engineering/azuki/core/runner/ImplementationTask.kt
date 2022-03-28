package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.System
import java.net.URLClassLoader


class ImplementationTask<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    R,
    >(
    private val implementationProvider: ImplementationProvider<AF, CF, QF, AGF>,
    private val taskName: String,
    private val task: (Implementation<AF, CF, QF, AGF, *>) -> R
) : Thread() {

    var result: Result<R>? = null
        private set

    override fun run() {
        val implementation = implementationProvider.getImplementation()
        val start = System.nanoTime()
        val outCapture = LogAndCaptureOutputStream { Log.info(it) }
        val errCapture = LogAndCaptureOutputStream { Log.error(it) }
        System.setOut(PrintStream(outCapture))
        System.setErr(PrintStream(errCapture))
        result = try {
            val result = task(implementation)
            val duration = System.nanoTime() - start
            Log.debug("Completed task '$taskName' in ${duration.formatNs()}")
            outCapture.flush()
            errCapture.flush()
            Result(taskName = taskName,
                result = result,
                duration = duration,
                output = outCapture.getCapturedText(),
                error = errCapture.getCapturedText(),
                implName = implementation.name)
        } catch (e: Exception) {
            Log.error("Unexpected error", e)
            val duration = System.nanoTime() - start
            outCapture.flush()
            Result(taskName = taskName,
                error = createErrorText(e),
                duration = duration,
                output = outCapture.getCapturedText(),
                implName = implementation.name)
        } finally {
            outCapture.close()
            errCapture.close()
            System.setOut(PrintStream(FileOutputStream(FileDescriptor.out)))
            System.setErr(PrintStream(FileOutputStream(FileDescriptor.err)))
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(ImplementationTask::class.java)

        private fun Long.formatNs(): String =
            if (this > 10_000_000_000L) {
                "${this / 1_000_000_000L}s"
            } else if (this > 10_000_000L) {
                "${this / 1_000_000L}ms"
            } else {
                "${this}ns"
            }

        fun createErrorText(e: Exception) =
            "${e.message}\n${e.stackTrace.joinToString(separator = "\n", limit = 10)}"
    }

    data class Result<T>(
        val taskName: String,
        val implName: String,
        val result: T? = null,
        val error: String? = null,
        val script: String? = null,
        val output: String? = null,
        val duration: Long? = null
    ) {
        fun durationMs() = if (duration == null) 0 else duration / 1000000
    }

}

private class LogAndCaptureOutputStream(
    private val log: (String) -> Unit
) : OutputStream() {
    private val capture = ByteArrayOutputStream()
    private val buffer = mutableListOf<Byte>()

    fun getCapturedText() = String(capture.toByteArray())

    override fun write(b: Int) {
        if (b.toChar() == '\n') {
            log(String(buffer.toByteArray()))
            buffer.clear()
        } else {
            buffer.add(b.toByte())
        }
        capture.write(b)
    }

    override fun flush() {
        capture.flush()
    }

    override fun close() {
        capture.close()
    }
}

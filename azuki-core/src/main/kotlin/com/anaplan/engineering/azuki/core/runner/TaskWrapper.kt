package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.JvmSystemProperties.redirectStdStreamsPropertyName
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.System

internal class TaskWrapper<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    R,
    >(
    private val taskType: TaskType,
    private val implementation: Implementation<AF, CF, QF, AGF, *>,
    private val task: (Implementation<AF, CF, QF, AGF, *>) -> R
) {

    fun <S : BuildableScenario<AF>> run(scenario: S): TaskResult<S, R> {
        val start = System.nanoTime()
        val redirectStdStreams = System.getProperty(redirectStdStreamsPropertyName, "false").toBoolean()
        val out = System.out
        val err = System.err
        val outCapture = LogAndCaptureOutputStream { if (redirectStdStreams) Log.info(it) else out.println(it) }
        val errCapture = LogAndCaptureOutputStream { if (redirectStdStreams) Log.error(it) else err.println(it) }
        System.setOut(PrintStream(outCapture))
        System.setErr(PrintStream(errCapture))
        return try {
            Log.debug("Running task type={}", taskType)
            val result = task(implementation)
            val duration = System.nanoTime() - start
            Log.debug("Completed task type={} duration={} result={}", taskType, duration.formatNs(), result)
            outCapture.flush()
            errCapture.flush()
            TaskResult(taskType = taskType,
                scenario = scenario,
                result = result,
                duration = duration,
                log = Log(
                    output = outCapture.getCapturedText(),
                    error = errCapture.getCapturedText()
                ),
                implName = implementation.name)
        } catch (e: Exception) {
            Log.error("Unexpected error running task type=${taskType}", e)
            val duration = System.nanoTime() - start
            outCapture.flush()
            TaskResult(taskType = taskType,
                scenario = scenario,
                exception = e,
                duration = duration,
                log = Log(
                    output = outCapture.getCapturedText(),
                    error = errCapture.getCapturedText()
                ),
                implName = implementation.name)
        } catch (t: Throwable) {
            Log.error("System error running task type=${taskType}", t)
            throw t
        } finally {
            System.setOut(out)
            System.setErr(err)
            // Try to avoid stdout/stderr pointing to closed streams by delaying closing them:
            outCapture.close()
            errCapture.close()
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(TaskWrapper::class.java)

        private fun Long.formatNs(): String =
            if (this > 10_000_000_000L) {
                "${this / 1_000_000_000L}s"
            } else if (this > 10_000_000L) {
                "${this / 1_000_000L}ms"
            } else {
                "${this}ns"
            }
    }
}

internal class LogAndCaptureOutputStream(
    private val log: (String) -> Unit
) : OutputStream() {

    private val capture = ByteArrayOutputStream()
    private val buffer = ByteArrayOutputStream()

    fun getCapturedText(): String = capture.toString(Charsets.UTF_8)

    override fun write(b: Int) {
        if (b.toChar() == '\n') {
            logAndClearBuffer()
        } else {
            buffer.write(b)
        }
        capture.write(b)
    }

    override fun flush() {
        buffer.flush()
        capture.flush()
    }

    override fun close() {
        if (buffer.size() != 0) {
            logAndClearBuffer()
        }
        buffer.close()
        capture.close()
    }

    private fun logAndClearBuffer() {
        try {
            log(buffer.toString(Charsets.UTF_8))
            buffer.reset()
        } catch (e: Exception) {
            Log.warn("Unable to capture stdout for task: {}", e.message)
            Log.warn("Stacktrace:\n{}", e.stackTraceToString())
        }
    }

    companion object {

        private val Log = LoggerFactory.getLogger(LogAndCaptureOutputStream::class.java)
    }
}

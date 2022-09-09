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
            Log.debug("Running task type=$taskType")
            val result = task(implementation)
            val duration = System.nanoTime() - start
            Log.debug("Completed task type=$taskType duration=${duration.formatNs()} result=$result")
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
            outCapture.close()
            errCapture.close()
            System.setOut(out)
            System.setErr(err)
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
        if (buffer.isNotEmpty()) {
            log(String(buffer.toByteArray()))
            buffer.clear()
        }
        capture.close()
    }
}

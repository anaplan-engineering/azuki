package com.anaplan.engineering.azuki.verify.orchestrator

import com.anaplan.engineering.azuki.core.JvmSystemProperties
import com.anaplan.engineering.azuki.runner.ExitCode
import org.slf4j.LoggerFactory
import java.nio.file.Files;
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class ForkedJvmRunner(private val orchestrationEnvironment: VerificationEnvironment) {

    private val classpathJar by lazy {
        val classpath = System.getProperty("java.class.path").split(File.pathSeparator)
            .joinToString(" ") { File(it).toURI().toString() }
        val manifest = Manifest()
        manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        manifest.mainAttributes[Attributes.Name.CLASS_PATH] = classpath
        val jarFile = File(Files.createTempDirectory("runnerClasspath").toFile(), "runnerClasspath.jar")
        jarFile.parentFile.mkdirs()
        JarOutputStream(FileOutputStream(jarFile), manifest).close()
        jarFile
    }

    // Kotlin script engine blows up with non-jars on classpath e.g. logging files
    private val scriptClasspath by lazy {
        System.getProperty("java.class.path").split(File.pathSeparator).filter { it.endsWith(".jar") }
            .joinToString(File.pathSeparator)
    }

    fun forkAndRun(
        feInstanceName: String,
        scenarioFile: File,
        importFile: File,
        oracleImplementationInstances: List<String>,
        testImplementationInstance: String,
        testPackageName: String?,
        testClassName: String?,
    ): ProcessResult {
        var encapsulatedTmp: File? = null
        if (orchestrationEnvironment.forkConfiguration.encapsulateTmp) {
            encapsulatedTmp = Files.createTempDirectory(feInstanceName).toFile()
        }
        val runDir = scenarioFile.parentFile
        val jvmArgs = mutableListOf<String>()
        // See https://youtrack.jetbrains.com/issue/KT-21443
        jvmArgs.add("-Dkotlin.script.classpath=$scriptClasspath")
        // propagate instance jars
        jvmArgs.add("-D${JvmSystemProperties.jarInstancesPropertyName}=${System.getProperty(JvmSystemProperties.jarInstancesPropertyName)}")
        if (encapsulatedTmp != null) {
            jvmArgs.add("-Djava.io.tmpdir=${encapsulatedTmp.absolutePath}")
        }
        jvmArgs.addAll(orchestrationEnvironment.forkConfiguration.jvmArgs)
        jvmArgs.addAll(orchestrationEnvironment.forkConfiguration.systemProperties.map { (k, v) -> "-D$k=$v" })

        if (orchestrationEnvironment.killAgentBinary != null && orchestrationEnvironment.killAgentBinary.exists()) {
            jvmArgs.add("-agentpath:${orchestrationEnvironment.killAgentBinary.absolutePath}")
        }

        val programArgs = oracleImplementationInstances.map { "-o" opt it } + listOf(
            "-d" opt runDir.absolutePath,
            "-p" opt (testPackageName ?: orchestrationEnvironment.testPackage),
            "-j" opt orchestrationEnvironment.junitResultsDir.absolutePath,
            "-v" opt orchestrationEnvironment.verifiedTestsDir.absolutePath,
            "-u" opt orchestrationEnvironment.unverifiedTestsDir.absolutePath,
            "-i" opt importFile.absolutePath,
            "-t" opt testImplementationInstance,
        ) + if (testClassName == null) {
            emptyList<JavaProcessContext.ProgramArgument>()
        } else {
            listOf("-c" opt testClassName)
        } + JavaProcessContext.Argument(scenarioFile.absolutePath)

        val javaProcessContext = JavaProcessContext(
            workingDirectory = runDir,
            mainClass = orchestrationEnvironment.forkConfiguration.runnerMainClass,
            javaHome = orchestrationEnvironment.javaHome,
            classpath = classpathJar.absolutePath,
            jvmArgs = jvmArgs,
            programArgs = programArgs,
            timeout = orchestrationEnvironment.forkConfiguration.timeout,
        )

        val processResult =
            try {
                runProcess(feInstanceName, javaProcessContext)
            } finally {
                if (encapsulatedTmp != null) {
                    Files.walkFileTree(encapsulatedTmp.toPath(), RecursiveDeleter)
                }
            }
        if (processResult.exitCode == ExitCode.TimeOut) {
            Log.warn("Process timed out scenario=$feInstanceName action=forkComplete")
        } else {
            Log.info("scenario=$feInstanceName action=forkComplete exitCode=${processResult.exitCode}")
        }
        return processResult
    }

    object RecursiveDeleter : SimpleFileVisitor<Path>() {
        override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
            Files.delete(file)
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            Files.delete(dir)
            return FileVisitResult.CONTINUE
        }
    }

    private fun runProcess(feInstanceName: String, context: JavaProcessContext): ProcessResult {
        if (Log.isDebugEnabled) {
            Log.debug("scenario=$feInstanceName action=forkLaunched command=`${context.commandLine}`${timeoutStr(context.timeout)}")
        } else {
            Log.info("scenario=$feInstanceName action=forkLaunched")
        }
        val processBuilder = ProcessBuilder()
        processBuilder.directory(context.workingDirectory)
        processBuilder.command(context.commandList)
        if (orchestrationEnvironment.forkConfiguration.environment.isNotEmpty()) {
            processBuilder.environment().putAll(orchestrationEnvironment.forkConfiguration.environment)
        }
        val process: Process?
        val terminated: Boolean
        try {
            process = processBuilder.start()
            terminated = if (context.timeout == null) {
                process.waitFor()
                true
            } else {
                process.waitFor(context.timeout, TimeUnit.SECONDS)
            }
        } catch (e: IOException) {
            return ProcessResult(
                success = false,
                exitCode = ExitCode.UnknownError,
                exception = e,
            )
        } catch (e: InterruptedException) {
            return ProcessResult(
                success = false,
                exitCode = ExitCode.UnknownError,
                exception = e,
            )
        }
        if (!terminated) {
            process.destroyForcibly()
            while (process.isAlive) {
                Thread.sleep(1000)
            }
            return ProcessResult(
                success = false,
                exitCode = ExitCode.TimeOut,
            )
        }
        if (process == null || process.exitValue() != 0) {
            return ProcessResult(
                success = false,
                exitCode = ExitCode.values().get(process.exitValue()),
            )
        }
        return ProcessResult(
            success = true,
            exitCode = ExitCode.Ok
        )
    }

    private fun timeoutStr(timeout: Long?) = if (timeout == null) "" else " timeout=${timeout}s"

    companion object {
        private val Log = LoggerFactory.getLogger(ForkedJvmRunner::class.java)
    }

    data class ProcessResult(
        val success: Boolean,
        val exitCode: ExitCode,
        val exception: Exception? = null,
    )

    data class JavaProcessContext(
        val javaHome: File,
        val workingDirectory: File,
        val mainClass: String,
        val classpath: String,
        val jvmArgs: List<String>,
        val programArgs: List<ProgramArgument>,
        val timeout: Long?,
    ) {
        companion object {
            val whiteSpaceRegex = Regex("\\s+")
        }

        private val jvmArgString = jvmArgs.joinToString(" ")
        private val javaBinary = "${javaHome.absolutePath}/bin/java"
        private val programArgsString = programArgs.joinToString(" ")

        val commandLine by lazy { "$javaBinary -cp $classpath $jvmArgString $mainClass $programArgsString" }

        val commandList by lazy { commandLine.split(whiteSpaceRegex) }

        interface ProgramArgument

        class ShortOption(val option: String, val value: String) : ProgramArgument {
            override fun toString() = "$option $value"
        }

        class Argument(val value: String) : ProgramArgument {
            override fun toString() = value
        }
    }

    infix fun String.opt(value: String) = JavaProcessContext.ShortOption(this, value)

}

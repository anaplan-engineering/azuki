package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.JvmSystemProperties.excludedImplementationsPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.includedImplementationsPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.jarInstancesPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.persistenceVerificationInstanceJarPropertyName
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader

interface ImplementationInstance<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    > {

    /**
     * The name of the general implementation e.g the 'Acme' implementation
     */
    val implementationName: String

    /**
     * The name of the instance -- we may have two versions of the same general implementation e.g. 'Acme v1.0.0` and
     * 'Acme v2.2.0' both would have the same implementation name, but must have different instance names
     */
    val instanceName: String

    val version: String?

    fun <S : BuildableScenario<AF>, R> runTask(
        taskType: TaskType,
        scenario: S,
        task: (Implementation<AF, CF, QF, AGF, *>) -> R
    ): TaskResult<S, R>

    companion object {

        private val Log = LoggerFactory.getLogger(JarImplementationInstance::class.java)

        private val implementationInstances = mutableListOf<ImplementationInstance<*, *, *, *>>()

        private val persistenceVerificationInstances = mutableListOf<ImplementationInstance<*, *, *, *>>()

        val havePersistenceVerificationInstance by lazy {
            java.lang.System.getProperty(persistenceVerificationInstanceJarPropertyName) != null
        }

        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> getPersistenceVerificationInstance(): ImplementationInstance<AF, CF, QF, AGF> {
            if (persistenceVerificationInstances.isEmpty()) {
                Log.debug("Loading persistence verification instance as required but not registered")
                persistenceVerificationInstances.addAll(getListProperty(persistenceVerificationInstanceJarPropertyName)!!.map {
                    JarImplementationInstance<AF, CF, QF, AGF>(File(it))
                })
                Log.debug("Loaded persistence verification instance instance=${
                    persistenceVerificationInstances.joinToString(", ") { it.instanceName }
                }")
            }
            return persistenceVerificationInstances.filterIsInstance<ImplementationInstance<AF, CF, QF, AGF>>().single()
        }

        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> getImplementationInstances(): List<ImplementationInstance<AF, CF, QF, AGF>> {
            if (implementationInstances.isEmpty()) {
                Log.debug("Loading implementation instances as none are registered")
                implementationInstances.addAll(InstanceFactory<AF, CF, QF, AGF>().createImplementationInstances())
                Log.debug("Loaded implementation instances instances=${implementationInstances.joinToString(", ") { it.instanceName }}")
            }
            return implementationInstances.filterIsInstance<ImplementationInstance<AF, CF, QF, AGF>>()
        }

        private fun getListProperty(propertyName: String): List<String>? {
            val property = java.lang.System.getProperty(propertyName)
            return if (property == null || property.isBlank()) {
                null
            } else {
                property.split(",").map { it.trim() }
            }
        }

        private class InstanceFactory<
            AF : ActionFactory,
            CF : CheckFactory,
            QF : QueryFactory,
            AGF : ActionGeneratorFactory,
            > {

            private fun haveJarInstances() = java.lang.System.getProperty(jarInstancesPropertyName) != null

            fun createImplementationInstances() =
                if (haveJarInstances()) {
                    Log.trace("Loading implementation instances using configured property with value: ${
                        java.lang.System.getProperty(jarInstancesPropertyName)
                    }")
                    createInstancesFromConfiguredJars()
                } else {
                    Log.trace("Loading implementation instances from classpath")
                    if (havePersistenceVerificationInstance) {
                        Log.warn("It not advised to mix classpath loading with the use of persistence verification instance. There may be unexpected classloading issues!")
                    }
                    createInstancesFromClasspath()
                }


            private fun List<ImplementationInstance<AF, CF, QF, AGF>>.filterImplementations(): List<ImplementationInstance<AF, CF, QF, AGF>> {
                val includedImplementations = getListProperty(includedImplementationsPropertyName)
                val excludedImplementations = getListProperty(excludedImplementationsPropertyName)
                return this.filter {
                    includedImplementations == null || it.implementationName in includedImplementations
                }.filter {
                    excludedImplementations == null || it.implementationName !in excludedImplementations
                }
            }

            private fun createInstancesFromClasspath() =
                Implementation.locateImplementations<AF, CF, QF, AGF>().map { StaticImplementationInstance(it) }
                    .filterImplementations()

            private fun createInstancesFromConfiguredJars() =
                getListProperty(jarInstancesPropertyName)!!.map {
                    JarImplementationInstance<AF, CF, QF, AGF>(File(it))
                }.filterImplementations()
        }
    }

}

class StaticImplementationInstance<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    private val implementation: Implementation<AF, CF, QF, AGF, *>
) : ImplementationInstance<AF, CF, QF, AGF> {

    override val implementationName = implementation.name

    override val instanceName = implementationName

    override val version = null

    override fun <S : BuildableScenario<AF>, R> runTask(
        taskType: TaskType,
        scenario: S,
        task: (Implementation<AF, CF, QF, AGF, *>) -> R
    ): TaskResult<S, R> {
        val implementationTask = TaskWrapper(taskType, implementation, task)
        return implementationTask.run(scenario)
    }

    override fun toString() = "$implementationName [Static]"
}

class JarImplementationInstance<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    private val jar: File
) : ImplementationInstance<AF, CF, QF, AGF> {

    init {
        if (!jar.exists() || !jar.isFile) {
            throw IllegalArgumentException("Implementation instance jar '$jar' is not available")
        }
    }

    private val classLoaderCache = mutableMapOf<ClassLoader, URLClassLoader>()

    override val implementationName = jar.nameWithoutExtension.split("-").first()

    override val instanceName = jar.nameWithoutExtension

    override val version = jar.nameWithoutExtension.split("-").getOrNull(1)

    private val threadGroup = ThreadGroup("implementation-$instanceName")

    override fun <S : BuildableScenario<AF>, R> runTask(
        taskType: TaskType,
        scenario: S,
        task: (Implementation<AF, CF, QF, AGF, *>) -> R
    ): TaskResult<S, R> {
        val runner = object : Runnable {
            var result: TaskResult<S, R>? = null

            override fun run() {
                val implementations = Implementation.locateImplementations<AF, CF, QF, AGF>()
                result = if (implementations.size == 1) {
                    val implementation = implementations.single()
                    Log.debug("Running task type={} implementation={}", taskType, implementation.name)
                    val implementationTask = TaskWrapper(taskType, implementation, task)
                    implementationTask.run(scenario)
                } else {
                    val exception =
                        IllegalStateException("Implementation instance jar should contain exactly one implementation, but ${implementations.size} found")
                    Log.error("Error running task type=$taskType instance=${instanceName}", exception)
                    TaskResult(
                        taskType = taskType,
                        implName = "Unknown",
                        result = null,
                        exception = exception,
                        scenario = scenario
                    )
                }
            }
        }
        // need to create thread from our own thread group or can clash with that created by JUnit's  FailOnTimeout
        val thread = Thread(threadGroup, runner, "implementation-task-$taskType")
        thread.contextClassLoader = getClassLoader(thread.contextClassLoader)
        thread.start()
        thread.join()
        return runner.result!!
    }

    private fun getClassLoader(parent: ClassLoader): ClassLoader {
        if (!classLoaderCache.containsKey(parent)) {
            Log.debug("Cache miss instance={} jar={} parentClassLoader={}", this, jar, parent)
            val jarUrl = jar.toURI().toURL()
            classLoaderCache[parent] = URLClassLoader(arrayOf(jarUrl), parent)
        }
        return classLoaderCache[parent]!!
    }

    override fun toString() = instanceName

    companion object {
        private val Log = LoggerFactory.getLogger(JarImplementationInstance::class.java)
    }
}

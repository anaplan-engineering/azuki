package com.anaplan.engineering.azuki.core.runner

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

    fun <R> runTask(
        taskName: String,
        task: (Implementation<AF, CF, QF, AGF, *>) -> R
    ): TaskResult<R>

    companion object {
        const val includedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.includes"
        const val excludedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.excludes"
        const val jarInstancesPropertyName = "com.anaplan.engineering.azuki.implementation.instance.jars"

        private val Log = LoggerFactory.getLogger(JarImplementationInstance::class.java)

        private val implementationInstances = mutableListOf<ImplementationInstance<*, *, *, *>>()

        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> getImplementationInstances(): List<ImplementationInstance<AF, CF, QF, AGF>> {
            if (implementationInstances.isEmpty()) {
                Log.debug("Loading implementation instances as none are registered")
                implementationInstances.addAll(InstanceFactory<AF, CF, QF, AGF>().createImplementationInstances())
            }
            return implementationInstances.filterIsInstance<ImplementationInstance<AF, CF, QF, AGF>>()
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
                    createInstancesFromConfiguredJars()
                } else {
                    createInstancesFromClasspath()
                }

            private fun getListProperty(propertyName: String): List<String>? {
                val property = java.lang.System.getProperty(propertyName)
                return if (property == null || property.isBlank()) {
                    null
                } else {
                    property.split(",").map { it.trim() }
                }
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

    override fun <R> runTask(taskName: String, task: (Implementation<AF, CF, QF, AGF, *>) -> R): TaskResult<R> {
        val implementationTask = TaskWrapper(taskName, implementation, task)
        return implementationTask.run()
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

    private val threadGroup = ThreadGroup("implementation-$instanceName")

    override fun <R> runTask(taskName: String, task: (Implementation<AF, CF, QF, AGF, *>) -> R): TaskResult<R> {
        val runner = object: Runnable {
            var result : TaskResult<R>? = null

            override fun run() {
                val implementation = Implementation.locateImplementations<AF, CF, QF, AGF>().single()
                val implementationTask = TaskWrapper(taskName, implementation, task)
                result = implementationTask.run()
            }
        }
        // need to create thread from our own thread group or can clash with that created by JUnit's  FailOnTimeout
        val thread = Thread(threadGroup, runner, "implementation-task-$taskName")
        thread.contextClassLoader = getClassloader(thread.contextClassLoader)
        thread.start()
        thread.join()
        return runner.result!!
    }

    private fun getClassloader(parent: ClassLoader): ClassLoader {
        if (!classLoaderCache.containsKey(parent)) {
            Log.debug("Cache miss for $jar, parent class loader $parent")
            val jarUrl = jar.toURI().toURL()
            classLoaderCache[parent] = URLClassLoader(arrayOf(jarUrl), parent)
        }
        return classLoaderCache[parent]!!
    }

    override fun toString() = "$implementationName [$jar]"

    companion object {
        private val Log = LoggerFactory.getLogger(JarImplementationInstance::class.java)
    }
}

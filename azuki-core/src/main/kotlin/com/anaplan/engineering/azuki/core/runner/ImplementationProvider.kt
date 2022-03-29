package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader

interface ImplementationProvider<
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
     * The name of the provider -- we may have two versions of the same general implementation e.g. 'Acme v1.0.0` and
     * 'Acme v2.2.0' both would have the same implementation name, but must have different provider names
     */
    val providerName: String

    fun getImplementation(): Implementation<AF, CF, QF, AGF, *>

    fun <R> createTask(
        taskName: String,
        task: (Implementation<AF, CF, QF, AGF, *>) -> R
    ) = ImplementationTask(this, taskName, task)

    companion object {
        const val includedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.includes"
        const val excludedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.excludes"
        const val providersPropertyName = "com.anaplan.engineering.azuki.implementation.providers"

        private val Log = LoggerFactory.getLogger(JarImplementationProvider::class.java)

        private val implementationProviders = mutableListOf<ImplementationProvider<*, *, *, *>>()

        fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> getImplementationProviders(): List<ImplementationProvider<AF, CF, QF, AGF>> {
            if (implementationProviders.isEmpty()) {
                Log.debug("Loading implementation providers as none are registered")
                implementationProviders.addAll(Loader<AF, CF, QF, AGF>().loadImplementationProviders())
            }
            return implementationProviders.filterIsInstance<ImplementationProvider<AF, CF, QF, AGF>>()
        }

        private class Loader<
            AF : ActionFactory,
            CF : CheckFactory,
            QF : QueryFactory,
            AGF : ActionGeneratorFactory,
            > {

            private fun haveProvidersConfiguration() = java.lang.System.getProperty(providersPropertyName) != null

            fun loadImplementationProviders() =
                if (haveProvidersConfiguration()) {
                    loadImplementationProvidersFromConfiguration()
                } else {
                    loadImplementationProvidersFromClasspath()
                }

            private fun getListProperty(propertyName: String): List<String>? {
                val property = java.lang.System.getProperty(propertyName)
                return if (property == null || property.isBlank()) {
                    null
                } else {
                    property.split(",").map { it.trim() }
                }
            }

            private fun List<ImplementationProvider<AF, CF, QF, AGF>>.filterImplementations(): List<ImplementationProvider<AF, CF, QF, AGF>> {
                val includedImplementations = getListProperty(includedImplementationsPropertyName)
                val excludedImplementations = getListProperty(excludedImplementationsPropertyName)
                return this.filter {
                    includedImplementations == null || it.implementationName in includedImplementations
                }.filter {
                    excludedImplementations == null || it.implementationName !in excludedImplementations
                }
            }

            private fun loadImplementationProvidersFromClasspath() =
                Implementation.locateImplementations<AF, CF, QF, AGF>().map { StaticImplementationProvider(it) }
                    .filterImplementations()

            private fun loadImplementationProvidersFromConfiguration() =
                getListProperty(providersPropertyName)!!.map {
                    JarImplementationProvider<AF, CF, QF, AGF>(File(it))
                }.filterImplementations()
        }
    }

}

class StaticImplementationProvider<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    private val implementation: Implementation<AF, CF, QF, AGF, *>
) : ImplementationProvider<AF, CF, QF, AGF> {

    override val implementationName = implementation.name

    override val providerName = implementationName

    override fun getImplementation() = implementation

    override fun toString() = "$implementationName [Static]"
}

class JarImplementationProvider<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    private val jar: File
) : ImplementationProvider<AF, CF, QF, AGF> {

    init {
        if (!jar.exists() || !jar.isFile) {
            throw IllegalArgumentException("Implementation provider '$jar' is not available")
        }
    }

    private val classLoaderCache = mutableMapOf<ClassLoader, URLClassLoader>()

    override val implementationName = jar.nameWithoutExtension.split("-").first()

    override val providerName = jar.nameWithoutExtension

    override fun getImplementation(): Implementation<AF, CF, QF, AGF, *> {
        val thread = Thread.currentThread()
        thread.contextClassLoader = getClassloader(thread.contextClassLoader)
        return Implementation.locateImplementations<AF, CF, QF, AGF>().single()
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
        private val Log = LoggerFactory.getLogger(JarImplementationProvider::class.java)
    }
}

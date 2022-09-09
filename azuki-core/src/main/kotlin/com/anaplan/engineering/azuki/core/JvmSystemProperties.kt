package com.anaplan.engineering.azuki.core

object JvmSystemProperties {

    /**
     * Comma separated list of implementations to explicitly include when running scenarios
     */
    const val includedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.includes"

    /**
     * Comma separated list of implementations to explicitly exclude when running scenarios
     */
    const val excludedImplementationsPropertyName = "com.anaplan.engineering.azuki.implementation.excludes"

    /**
     * Comma separated list of jar files containing implementation instances
     *
     * Used when classloading implementations dynamically
     */
    const val jarInstancesPropertyName = "com.anaplan.engineering.azuki.implementation.instance.jars"

    /**
     * If non-zero the default timeout in minutes to apply to scenarios run with JUnit
     *
     * Default: 3
     */
    const val junitTimeoutPropertyName = "com.anaplan.engineering.azuki.junit.timeout"

    /**
     * If true the implementation name will be excluded from the description of JUnit tests
     *
     * Default:false
     *
     * N.B. if using more than one implementation, setting this property to truw will cause
     * JUnit report files from the same test, but with different implementations to overwrite
     * each other
     */
    const val excludeImplFromEacDescriptionPropertyName = "com.anaplan.engineering.azuki.junit.excludeImplName"

    /**
     * If true JUnit scenarios annotated with KnownBug will be run rather than skipped
     *
     * Default: false
     */
    const val forceKnownBugsPropertyName = "com.anaplan.engineering.azuki.junit.runKnownBugs"

    /**
     * If true will redirect stdout & stderr to log
     *
     * Default: false
     */
    const val redirectStdStreamsPropertyName = "com.anaplan.engineering.azuki.task.redirectStdStreams"

    /**
     * If present Azuki will write EAC metadata to the specified directory
     */
    const val eacMetadataDirPropertyName = "com.anaplan.engineering.azuki.eac.metadataDir"
}

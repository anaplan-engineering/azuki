package com.anaplan.engineering.azuki.vdm.coverage

import com.anaplan.engineering.vdmanimation.api.AnimationCoverage
import com.anaplan.engineering.vdmanimation.api.Location
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class CoverageAggregator {

    private val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(LocationKeyDeserializerModule())

    fun aggregate(coverageDirectory: File) =
            aggregate(locateFilesWithExtension(coverageDirectory, "cov")
                    .map { objectMapper.readValue<AnimationCoverage>(it) })

    fun aggregate(coverageResults: List<AnimationCoverage>) =
            coverageResults.fold(AnimationCoverage(files = emptyList())) { l, r -> l.combine(r) }


    private fun locateFilesWithExtension(directory: File, vararg extensions: String): List<File> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        val files = ArrayList<Path>()
        val fileVisitor = object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                if (extensions.any { file.fileName.toString().endsWith(it) }) {
                    files.add(file)
                }
                return FileVisitResult.CONTINUE
            }
        }
        Files.walkFileTree(directory.toPath(), fileVisitor)
        return files.map { it.toFile() }
    }

}

private class LocationKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, context: DeserializationContext): Any {
        return if (key.startsWith("Location(") && key.endsWith(")")) {
            var startLine = -1
            var startPos = -1
            var endLine = -1
            var endPos = -1
            val fields = key.replace("Location(", "").dropLast(1).split(",")
            fields.forEach { field ->
                val (name, value) = field.split("=")
                val intValue = value.toInt()
                when (name.trim()) {
                    "startLine" -> startLine = intValue
                    "startPos" -> startPos = intValue
                    "endLine" -> endLine = intValue
                    "endPos" -> endPos = intValue
                }
            }
            Location(startLine, startPos, endLine, endPos)
        } else {
            throw IllegalStateException("Pair() expects a serialized format of '(first,second)', cannot understand '$key'")
        }
    }
}

private class LocationKeyDeserializerModule : SimpleModule() {
    init {
        addKeyDeserializer(Location::class.java, LocationKeyDeserializer())
    }
}

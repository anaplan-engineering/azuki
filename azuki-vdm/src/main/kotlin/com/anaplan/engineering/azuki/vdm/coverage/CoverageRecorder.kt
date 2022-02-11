package com.anaplan.engineering.azuki.vdm.coverage

import com.anaplan.engineering.vdmanimation.api.AnimationCoverage
import com.anaplan.engineering.vdmanimation.api.FileCoverage
import com.anaplan.engineering.vdmanimation.api.Location
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

object CoverageRecorder {

    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun recordJson(coverage: AnimationCoverage, coverageLocation: String? = System.getProperty("coverage.location")) {
        if (coverageLocation == null) return
        val coverageDir = File(coverageLocation)
        if (!coverageDir.exists()) {
            coverageDir.mkdirs()
        }
        objectMapper.writeValue(File(coverageDir, "${System.currentTimeMillis()}.cov"), coverage)
    }

    fun recordHtml(coverage: AnimationCoverage, reportLocation: String? = System.getProperty("report.location")) {
        if (reportLocation == null) return
        recordHtml(coverage, File(reportLocation))
    }

    fun recordHtml(coverage: AnimationCoverage, reportDir: File) {
        if (!reportDir.exists()) {
            reportDir.mkdirs()
        }
        coverage.files.groupBy { it.moduleName }.forEach { (moduleName, files) ->
            if (files.size == 1) {
                writeHtmlCoverage(reportDir, moduleName, files[0])
            } else {
                files.forEachIndexed { index, file -> writeHtmlCoverage(reportDir, "${moduleName}_$index", file) }
            }
        }
    }

    private val locationComparator = Comparator<Location> { o1, o2 ->
        val startLineCompare = o1.startLine.compareTo(o2.startLine)
        if (startLineCompare != 0) {
            startLineCompare
        } else {
            val startPosCompare = o1.startPos.compareTo(o2.startPos)
            if (startPosCompare != 0) {
                startPosCompare
            } else {
                val endLineCompare = o1.endLine.compareTo(o2.endLine)
                if (endLineCompare != 0) {
                    endLineCompare
                } else {
                    o1.endPos.compareTo(o2.endPos)
                }
            }
        }
    }

    private fun writeHtmlCoverage(reportDir: File, moduleName: String, fileCoverage: FileCoverage) {
        val reportFile = File(reportDir, "$moduleName.html")
        reportFile.writeText(generateHtml(fileCoverage))
    }


    private fun escape(text: String) =
            text
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")

    private fun generateHtml(fileCoverage: FileCoverage): String {
        val lines = fileCoverage.text.lines()
        val locations = fileCoverage.coverage.keys.sortedWith(locationComparator)
        var currentLine = 1
        var currentPos = 1
        val htmlBuilder = StringBuilder("<html>\n<pre>\n")
        fun copyText(startLine: Int, startPos: Int, endLine: Int, endPos: Int) {
            (startLine - 1..endLine - 1).forEach { line ->
                val lastIndex = lines[line].length
                val startIndex = if (line == (startLine - 1)) maxOf(startPos - 1, 0) else 0
                val endIndex = if (line == (endLine - 1)) maxOf(endPos - 1, 0) else lastIndex
                if (startIndex == lastIndex) {
                    htmlBuilder.append("\n")
                } else if (endIndex == lastIndex) {
                    htmlBuilder.append(escape(lines[line].substring(startIndex)))
                    htmlBuilder.append("\n")
                } else {
                    htmlBuilder.append(escape(lines[line].substring(startIndex, endIndex)))
                }
            }
        }
        locations.forEach { location ->
            val hitCount = fileCoverage.coverage[location] as Long
            if (location.startLine < currentLine || (location.startLine == currentLine && location.startPos < currentPos)) {
                throw IllegalStateException("Overlapping locations!")
            }
            if (location.startLine != currentLine || location.startPos != currentPos) {
                copyText(currentLine, currentPos, location.startLine, location.startPos)
            }
            if (hitCount > 0) {
                htmlBuilder.append("<span title=\"Hit $hitCount times\" style=\"background-color: #c4ffc5;\">")
                copyText(location.startLine, location.startPos, location.endLine, location.endPos)
                htmlBuilder.append("</span>")
            } else {
                htmlBuilder.append("<span style=\"background-color: #ff9696;\">")
                copyText(location.startLine, location.startPos, location.endLine, location.endPos)
                htmlBuilder.append("</span>")
            }
            currentLine = location.endLine
            currentPos = location.endPos
        }
        if (currentLine < lines.size || currentPos < lines[lines.size - 1].length) {
            copyText(currentLine, currentPos, lines.size, lines[lines.size - 1].length + 1)
        }

        htmlBuilder.append("\n</pre>\n</html>")
        return htmlBuilder.toString()
    }

}

fun main() {
    val coverage = CoverageAggregator().aggregate(File("/home/si/git/fspec-tools/oracle-vdm-acceptance-test/build/coverage"))
    CoverageRecorder.recordHtml(coverage, "/home/si/git/fspec-tools/oracle-vdm-acceptance-test/build/coverage/reports")
}

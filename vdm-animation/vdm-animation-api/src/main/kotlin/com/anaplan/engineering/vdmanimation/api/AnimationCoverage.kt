package com.anaplan.engineering.vdmanimation.api

data class AnimationCoverage(
        val files: List<FileCoverage>
) {
    fun combine(other: AnimationCoverage): AnimationCoverage {
        val textsInThis = this.files.map { it.text }
        val textsInOther = other.files.map { it.text }
        val filesJustInThis = this.files.filter { !textsInOther.contains(it.text) }
        val filesJustInOther = other.files.filter { !textsInThis.contains(it.text) }
        val combinedResults = (files - filesJustInThis).map { thisFile ->
            val otherFile = other.files.find { it.text == thisFile.text }
                    ?: throw IllegalStateException()
            val coverage = mutableMapOf<Location, Long>()
            coverage.putAll(thisFile.coverage)
            otherFile.coverage.forEach { (location, count) ->
                coverage[location] = count + (coverage[location] ?: 0)
            }
            FileCoverage(thisFile.moduleName, thisFile.text, coverage)
        }
        return AnimationCoverage(
                filesJustInThis + filesJustInOther + combinedResults
        )
    }

}

data class FileCoverage(
        val moduleName: String,
        val text: String,
        val coverage: Map<Location, Long>
)

data class Location(
        val startLine: Int,
        val startPos: Int,
        val endLine: Int,
        val endPos: Int
)


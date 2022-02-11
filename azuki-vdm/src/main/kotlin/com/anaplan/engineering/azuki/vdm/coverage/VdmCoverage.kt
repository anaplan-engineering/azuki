package com.anaplan.engineering.azuki.vdm.coverage

import com.anaplan.engineering.vdmanimation.api.AnimationCoverage
import com.anaplan.engineering.vdmanimation.api.FileCoverage

enum class AreaType : Coverage.AreaType {
    SPECIFICATION,
    MODULE,
    LOCATION
}

interface VdmCoverage : Coverage {
    val animationCoverage: AnimationCoverage
}

private val pairSum = { p1: Pair<Int, Int>, p2: Pair<Int, Int> -> p1.first + p2.first to p1.second + p2.second }

class SpecificationCoverage(
    override val animationCoverage: AnimationCoverage
) : VdmCoverage {

    private val moduleToFileCoverage: Map<String, List<FileCoverage>> by lazy {
        animationCoverage.files.groupBy { it.moduleName }
    }

    override val name = "VDM Specification"
    override val areaType = AreaType.SPECIFICATION
    override val childAreaType = AreaType.MODULE
    override val childAreaCount = moduleToFileCoverage.size.toLong()
    override val childAreaHitCount = moduleToFileCoverage.filter { (_, files) ->
        files.any { file ->
            file.coverage.values.any { it > 0 }
        }
    }.size.toLong()

    override val childCoverage: List<VdmCoverage> by lazy {
        moduleToFileCoverage.map { (module, files) ->
            ModuleCoverage(
                animationCoverage = AnimationCoverage(files),
                name = module
            )
        }
    }

    override fun getDefaultCoveragePc(): Double {
        val hitTotalPair = moduleToFileCoverage.map { (_, files) ->
            files.asSequence().map {
                it.coverage.filter { (_, v) -> v > 0 }.size to it.coverage.size
            }.fold(0 to 0, pairSum)
        }.fold(0 to 0, pairSum)
        return hitTotalPair.first.toDouble() / hitTotalPair.second
    }
}


class ModuleCoverage(
    override val animationCoverage: AnimationCoverage,
    override val name: String
) : VdmCoverage {

    override fun getDefaultCoveragePc() =
        childAreaHitCount.toDouble() / childAreaCount

    override val areaType = AreaType.MODULE
    override val childAreaType = AreaType.LOCATION
    override val childAreaCount = animationCoverage.files.map { it.coverage.size }.sum().toLong()
    override val childAreaHitCount =
        animationCoverage.files.map { it.coverage.filter { (_, v) -> v > 0 }.size }.sum().toLong()
    override val childCoverage: List<Coverage> = emptyList()

}

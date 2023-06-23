package com.anaplan.engineering.azuki.vdm.coverage

import com.anaplan.engineering.azuki.vdm.coverage.CoverageAggregator
import com.anaplan.engineering.azuki.vdm.coverage.SpecificationCoverage
import com.anaplan.engineering.azuki.vdm.coverage.VdmCoverage

class VdmCoverageManager {

    private val coverageAggregator = CoverageAggregator()

    /**
     * Even if we have two modules that cover the same module we always return a coverage at the structure
     * level.
     */
    fun aggregate(c1: VdmCoverage, c2: VdmCoverage) =
            SpecificationCoverage(coverageAggregator.aggregate(listOf(c1.animationCoverage, c2.animationCoverage)))

}

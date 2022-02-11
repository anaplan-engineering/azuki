package com.anaplan.engineering.azuki.vdm.coverage


interface Coverage {

    interface AreaType {
        val name: String
    }

    /**
     * The name of the covered area
     */
    val name: String

    /**
     * The type of the covered area
     */
    val areaType: AreaType

    /**
     * The type of areas into which this area is broken down
     */
    val childAreaType: AreaType

    /**
     * The total count of areas of type childAreaType that are in this area
     */
    val childAreaCount: Long

    /**
     * The count of areas of type childAreaType that are in this area and which have been covered
     */
    val childAreaHitCount: Long

    /**
     * The details of coverage for child areas (will be empty if the area cannot be broken down further)
     */
    val childCoverage: List<Coverage>

    fun getDefaultCoveragePc(): Double

}

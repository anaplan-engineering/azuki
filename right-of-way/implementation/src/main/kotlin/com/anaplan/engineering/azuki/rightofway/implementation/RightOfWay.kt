package com.anaplan.engineering.azuki.rightofway.implementation

interface RightOfWay {

    enum class Direction { PARALLEL, SAME, OPPOSITE }
    enum class Convergence { CONVERGING, DIVERGING, OVERTAKING }

    fun inQ1(aircraft: Aircraft, position: Position): Boolean
    fun inQ2(aircraft: Aircraft, position: Position): Boolean
    fun inQ3(aircraft: Aircraft, position: Position): Boolean
    fun inQ4(aircraft: Aircraft, position: Position): Boolean
    fun quadrantConvergence(aircraft1: Aircraft, aircraft2: Aircraft): Convergence

    fun track(aircraft: Aircraft): Double
    fun timeToClosestPointApproach(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun horizontalMissDistance(aircraft1: Aircraft, aircraft2: Aircraft): Double

    fun toTheLeftOf(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun toTheRightOf(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun leftOf(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun rightOf(aircraft1: Aircraft, aircraft2: Aircraft): Boolean

    fun crossing(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun zeroCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun oneCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun bothCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean

    fun orientation(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun direction(aircraft1: Aircraft, aircraft2: Aircraft): Direction

    fun converging(aircraft1: Aircraft, aircraft2: Aircraft, delta_c: Double): Boolean
    fun headon(aircraft1: Aircraft, aircraft2: Aircraft, delta_c: Double, Theta_h: Double): Boolean
    fun convergingNotHeadon(aircraft1: Aircraft, aircraft2: Aircraft, delta_c: Double, Theta_h: Double): Boolean
    fun overtaking(aircraft1: Aircraft, aircraft2: Aircraft, delta_o: Double): Boolean
    fun hasRightOfWay(aircraft1: Aircraft, aircraft2: Aircraft, delta_c: Double, delta_o: Double, Theta_h: Double): Boolean
}

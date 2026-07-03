package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action

interface WorldActions : WorldDeclarableActions {
    fun removeAPurse(worldName: String, purseName: String)
}

interface WorldDeclarableActions {
    fun addANewPurse(worldName: String, purseName: String)


}

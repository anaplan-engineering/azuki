package com.anaplan.engineering.azuki.mondex.dsl.declaration

import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Status

interface PurseDeclarations {
    //LF @EK if AbPurse has ULong, why Int here?
    fun thereIsAPurse(purseName: String, balance: Int, lost: Int)
    //LF @EK concrete purses need a way of creating them. Could have `thereIsAnAbstractPurse` / `thereIsAConcretePurse` if clearerer
    fun thereIsAPurse(purseName: String, balance: ULong, exLog: Set<PayDetails>, //name: String,
                      nextSeqNo: ULong,
                      pdAuth: PayDetails,
                      status: Status)
}

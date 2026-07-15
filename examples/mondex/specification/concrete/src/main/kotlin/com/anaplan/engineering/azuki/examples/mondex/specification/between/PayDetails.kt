package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.Module

@Module
interface PayDetails : TransferDetails {
    val fromSeqNo: nat
    val toSeqNo: nat

    @Invariant
    fun namesDistinct() = from != to
}

//TODO LF does this need the toPurse.nextSeqNo instead?
fun PayDetails.startFrom(): Message = mk_StartFrom(mk_CounterPartyDetails(to, value, toSeqNo))
fun PayDetails.startTo(): Message = mk_StartTo(mk_CounterPartyDetails(from, value, fromSeqNo))

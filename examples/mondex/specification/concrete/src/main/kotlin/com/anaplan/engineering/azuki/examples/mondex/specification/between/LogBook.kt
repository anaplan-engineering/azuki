package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.as_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.powerset
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.mk_

@Module
interface LogBook : Relation<Name, PayDetails>


//LF @QST is this the best/right way here? Or better to have powerset in Kazuki for KSet/Relation?
fun logbook(pds: Set<PayDetails>): Set<LogBook> =
    (pds.flatMap { pd -> listOf(mk_(pd.from, pd), mk_(pd.to, pd)) }.toSet())
        .powerset()
        .map(::as_Relation)
        .map(::as_LogBook)
        .toSet()

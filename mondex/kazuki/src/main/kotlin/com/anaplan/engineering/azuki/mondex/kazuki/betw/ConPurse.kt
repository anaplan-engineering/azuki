package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.Purse
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.nat

enum class Status { eaFrom, eaTo, epr, epv, epa }

@Module
interface ConPurse : Purse {
    val exLog: Set<PayDetails>
    val name: Name
    val nextSeqNo: nat

    //LF: ConPurse invariant says the name must be in from or to. This represents the "last" payment done by this purse
    //    To bootstrap (first purse), you might need to have here something that might be null, given you can't have a
    //    payment to yourself. Or allow only when status = eaFrom?
    val pdAuth: PayDetails?
    val status: Status
}



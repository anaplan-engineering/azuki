package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.kazuki.core.*

//LF @EK This purse is to match the adapter-api Purse with its different implementations?
@Module
interface Purse {
    val balance: nat

    //LF @QST: how to implement Z hidding? Use reflection and keep a record of what can be modified?
    // something like val hidden: Set<Name> ?
}

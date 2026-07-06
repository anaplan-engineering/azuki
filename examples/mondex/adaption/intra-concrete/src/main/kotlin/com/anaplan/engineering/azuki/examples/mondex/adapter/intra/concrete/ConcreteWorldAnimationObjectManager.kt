package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.PurseDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.TransferDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.mk_BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Bottom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld_Module.mk_ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.mk_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ReadExceptionLog
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom_Module.mk_StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo_Module.mk_StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem_Module.mk_ConSystem
import com.anaplan.engineering.kazuki.core.*

data class TransferData(
    val name: String,
    val from: String,
    val to: String,
    val amount: Int,
    val status: Status
) {
    enum class Status {
        Created,
        Requested,
        Sent,
        Acknowledged,
        Aborted
    }

    val details by lazy {
        mk_TransferDetails(
            from.toName(),
            to.toName(),
            amount.toULong()
        )
    }

    val payDetails by lazy {
        mk_PayDetails(
            from.toName(),
            to.toName(),
            amount.toULong(),
            0uL,
            0uL
        )
    }
}

class ConcreteWorldAnimation(
    var system: BetweenSystem,//ConSystem,
    val worldName: String,
    val transfers: MutableMap<String, TransferData>
) {

    fun getWorld(worldName: String): ConWorld {
        require(worldName == this.worldName)
        return system.world
    }

    fun getPurse(name: String) = system.world.conAuthPurse[name.toName()]

    fun createTransfer(data: TransferData) {
        require(data.name !in transfers.keys && data.status == TransferData.Status.Created)
        transfers[data.name] = data
        system = system.functions.startTransfer(data.payDetails)
    }

    fun requestTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Created)
        updateStatus(transferName, TransferData.Status.Requested)
        val data = transfers[transferName]!!
        system = system.functions.requestTransfer(data.payDetails)
    }

    fun sendTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Requested)
        updateStatus(transferName, TransferData.Status.Sent)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.sendTransfer(data.details)
    }

    private fun updateStatus(transferName: String, status: TransferData.Status) {
        transfers[transferName] = transfers[transferName]!!.copy(status = status)
    }

    fun acknowledgeTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Sent)
        updateStatus(transferName, TransferData.Status.Acknowledged)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.acknowledgeTransfer(data.details)
    }


    fun abortTransfer(transferName: String) {
        require(transferName in transfers.keys)
        updateStatus(transferName, TransferData.Status.Aborted)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.abortTransfer(data.details)
    }

}

fun PurseDeclaration.toCounterPartyDetails(nextSeqNo: Int = 0) =
    mk_CounterPartyDetails(name.toName(), balance.toNat(), nextSeqNo.toNat())

class ConcreteWorldAnimationBuilder(private val declarations: List<Declaration>) {

    fun build(): ConcreteWorldAnimation {
        val worldDeclarations = declarations.filterIsInstance<WorldDeclaration>()
        require(worldDeclarations.size <= 1)
        val purseDeclarations = declarations.filterIsInstance<PurseDeclaration>()
        val purses = as_InjectiveMapping(purseDeclarations.map { dec ->
            mk_(dec.name.toName(),
                mk_ConPurse(
                    dec.balance.toULong(),
                    mk_Set(),
                    dec.name.toName(),
                    0uL,
                    null,
                    Status.eaFrom
                )
            )
        })
        // PRG126 as per 6.1 BetweenInitState
        //@4paper - ?
        val ether = mk_Set(Bottom, ReadExceptionLog) //+
//            set(purseDeclarations) { dec -> mk_StartFrom(dec.toCounterPartyDetails()) } +
//            set(purseDeclarations) { dec -> mk_StartTo(dec.toCounterPartyDetails()) }
        return ConcreteWorldAnimation(
            //system = mk_ConSystem(mk_ConWorld(purses, ether, mk_LogBook())),
            system = mk_BetweenSystem(mk_BetweenWorld(purses, ether, mk_LogBook())),

            transfers = declarations.filterIsInstance<TransferDeclaration>().associate { dec ->
                dec.name to TransferData(dec.name, dec.from, dec.to, dec.amount, TransferData.Status.Created)
            }.toMutableMap(),
            worldName = worldDeclarations.singleOrNull()?.name ?: "DEFAULT"
        )
    }

}

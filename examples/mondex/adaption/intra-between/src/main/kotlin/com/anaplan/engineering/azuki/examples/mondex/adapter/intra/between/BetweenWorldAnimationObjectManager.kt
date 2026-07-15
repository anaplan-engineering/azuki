package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.PurseDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.TransferDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.examples.mondex.specification.WorldSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenSystem_Module.mk_BetweenSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld_Module.mk_BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Bottom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails_Module.mk_CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.mk_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ReadExceptionLog
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.kazuki.core.*
import javax.management.monitor.StringMonitor

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

abstract class WorldAnimation<World, S: WorldSystem>(
    open var system: S,  // TODO: Change to WorldSystem when ConSystem finished
    val worldName: String,
    open val transfers: MutableMap<String, TransferData>
) {

    abstract fun getWorld(worldName: String): World
    abstract fun getPurse(name: String): ConPurse
    abstract fun createTransfer(data: TransferData)
    abstract fun requestTransfer(transferName: String)
    abstract fun sendTransfer(transferName: String)
    abstract fun acknowledgeTransfer(transferName: String)
    abstract fun abortTransfer(transferName: String)
}

open class BetweenWorldAnimation(
    override var system: BetweenSystem,
    worldName: String,
    transfers: MutableMap<String, TransferData>
) : WorldAnimation<BetweenWorld, BetweenSystem>(system, worldName, transfers) {

    override fun getWorld(worldName: String): BetweenWorld {
        require(worldName == this.worldName)
        return system.world
    }

    override fun getPurse(name: String) = system.world.conAuthPurse[name.toName()]

    override fun createTransfer(data: TransferData) {
        require(data.name !in transfers.keys && data.status == TransferData.Status.Created)
        transfers[data.name] = data
        system = system.functions.startTransfer(data.payDetails)
    }

    override fun requestTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Created)
        updateStatus(transferName, TransferData.Status.Requested)
        val data = transfers[transferName]!!
        system = system.functions.requestTransfer(data.payDetails)
    }

    private fun updateStatus(transferName: String, status: TransferData.Status) {
        transfers[transferName] = transfers[transferName]!!.copy(status = status)
    }

    override fun sendTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Requested)
        updateStatus(transferName, TransferData.Status.Sent)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.sendTransfer(data.details)
    }

    override fun acknowledgeTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Sent)
        updateStatus(transferName, TransferData.Status.Acknowledged)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.acknowledgeTransfer(data.details)
    }


    override fun abortTransfer(transferName: String) {
        require(transferName in transfers.keys)
        updateStatus(transferName, TransferData.Status.Aborted)
        val data = transfers[transferName]!!
        throw LateDetectUnsupportedActionException()
// TODO       system = system.functions.abortTransfer(data.details)
    }
}

fun PurseDeclaration.toCounterPartyDetails(nextSeqNo: Int = 0) =
    mk_CounterPartyDetails(name.toName(), balance.toNat(), nextSeqNo.toNat())

class BetweenWorldAnimationBuilder(private val declarations: List<Declaration>) {

    fun build(): BetweenWorldAnimation {
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
        return BetweenWorldAnimation(
            //system = mk_ConSystem(mk_ConWorld(purses, ether, mk_LogBook())),
            system = mk_BetweenSystem(mk_BetweenWorld(purses, ether, mk_LogBook())),

            transfers = declarations.filterIsInstance<TransferDeclaration>().associate { dec ->
                dec.name to TransferData(dec.name, dec.from, dec.to, dec.amount, TransferData.Status.Created)
            }.toMutableMap(),
            worldName = worldDeclarations.singleOrNull()?.name ?: "DEFAULT"
        )
    }

}

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
import com.anaplan.engineering.azuki.examples.mondex.specification.between.startFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.startTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.last
import javax.management.monitor.StringMonitor
import kotlin.require

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

abstract class WorldAnimation<W: ConWorld, S: WorldSystem>(
    open var system: S,
    val worldName: String,
    open val transfers: MutableMap<String, TransferData>
) {

    abstract fun getWorld(worldName: String): W
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
        val pd = data.payDetails
        system = system.functions.startTransferFrom(pd.from, pd.startFrom())
        system = system.functions.startTransferTo(pd.to, pd.startTo())
    }

    override fun requestTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Created)
        updateStatus(transferName, TransferData.Status.Requested)
        val data = transfers[transferName]!!
        system = system.functions.requestTransfer(data.payDetails.from, system.last)
    }

    private fun updateStatus(transferName: String, status: TransferData.Status) {
        transfers[transferName] = transfers[transferName]!!.copy(status = status)
    }

    override fun sendTransfer(transferName: String) {
        require(transfers[transferName]?.status in setOf(TransferData.Status.Requested))
        updateStatus(transferName, TransferData.Status.Sent)
        val data = transfers[transferName]!!
        system = system.functions.sendTransfer(data.payDetails.to, system.last)
    }

    override fun acknowledgeTransfer(transferName: String) {
        require(transfers[transferName]?.status in  setOf(TransferData.Status.Requested, TransferData.Status.Sent))
        updateStatus(transferName, TransferData.Status.Acknowledged)
        val data = transfers[transferName]!!
        system = system.functions.ackTransfer(data.payDetails.from, system.last)
    }


    override fun abortTransfer(transferName: String) {
        require(transferName in transfers.keys)
        updateStatus(transferName, TransferData.Status.Aborted)
        val data = transfers[transferName]!!
        system = system.functions.abortTransfer(data.payDetails.from, system.last)
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
            system = mk_BetweenSystem(mk_BetweenWorld(purses, ether, mk_LogBook()), Bottom),

            transfers = declarations.filterIsInstance<TransferDeclaration>().associate { dec ->
                dec.name to TransferData(dec.name, dec.from, dec.to, dec.amount, TransferData.Status.Created)
            }.toMutableMap(),
            worldName = worldDeclarations.singleOrNull()?.name ?: "DEFAULT"
        )
    }

}

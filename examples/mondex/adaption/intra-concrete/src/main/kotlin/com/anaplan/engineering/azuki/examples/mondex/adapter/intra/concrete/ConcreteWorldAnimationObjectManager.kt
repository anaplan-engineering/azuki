package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.PurseDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.TransferDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld_Module.mk_ConWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook_Module.mk_LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.Status
import com.anaplan.engineering.azuki.examples.mondex.specification.between.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.toName
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConSystem
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
}

class ConcreteWorldAnimation(
    var system: ConSystem,
    val worldName: String,
    val transfers: MutableMap<String, TransferData>
) {

    fun getWorld(worldName: String): ConWorld {
        require(worldName == this.worldName)
        return system.conWorld
    }

    fun getPurse(name: String) = system.conWorld.conAuthPurse[name.toName()]

    fun createTransfer(data: TransferData) {
        require(data.name !in transfers.keys && data.status == TransferData.Status.Created)
        transfers[data.name] = data
        // noop in concrete system
    }

    fun requestTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Created)
        updateStatus(transferName, TransferData.Status.Requested)
        val data = transfers[transferName]!!
        system = system.functions.startTransfer(data.details)
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

class ConcreteWorldAnimationBuilder(private val declarations: List<Declaration>) {

    fun build(): ConcreteWorldAnimation {
        val worldDeclarations = declarations.filterIsInstance<WorldDeclaration>()
        require(worldDeclarations.size <= 1)
        val purses = as_InjectiveMapping(declarations.filterIsInstance<PurseDeclaration>().map { dec ->
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
        return ConcreteWorldAnimation(
            system = mk_ConSystem(mk_ConWorld(purses, mk_Set(), mk_LogBook())),
            transfers = declarations.filterIsInstance<TransferDeclaration>().associate { dec ->
                dec.name to TransferData(dec.name, dec.from, dec.to, dec.amount, TransferData.Status.Created)
            }.toMutableMap(),
            worldName = worldDeclarations.singleOrNull()?.name ?: "DEFAULT"
        )
    }

}

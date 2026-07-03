package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.PurseDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.TransferDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbPurse_Module.mk_AbPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbSystem_Module.mk_AbSystem
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbWorld_Module.mk_AbWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.Transfer_Module.mk_Transfer
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.toName
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

class AbstractWorldAnimation(
    var system: AbSystem,
    val worldName: String,
    val transfers: MutableMap<String, TransferData>
) {

    fun getWorld(worldName: String): AbWorld {
        require(worldName == this.worldName)
        return system.abWorld
    }

    fun getPurse(name: String) = system.abWorld.abAuthPurse[name.toName()]

    fun createTransfer(data: TransferData) {
        require(data.name !in transfers.keys && data.status == TransferData.Status.Created)
        transfers[data.name] = data
        // noop in abstract system
    }

    fun requestTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Created)
        updateStatus(transferName, TransferData.Status.Requested)
        // noop in abstract system
    }

    fun sendTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Requested)
        updateStatus(transferName, TransferData.Status.Sent)
        // noop in abstract system
    }

    private fun updateStatus(transferName: String, status: TransferData.Status) {
        transfers[transferName] = transfers[transferName]!!.copy(status = status)
    }

    fun acknowledgeTransfer(transferName: String) {
        require(transfers[transferName]?.status == TransferData.Status.Sent)
        updateStatus(transferName, TransferData.Status.Acknowledged)
        val data = transfers[transferName]!!
        system = system.functions.successfulTransfer(data.details)
    }


    fun abortTransfer(transferName: String) {
        require(transferName in transfers.keys)
        val data = transfers[transferName]!!
        when (data.status) {
            TransferData.Status.Sent -> {
                // Aborted after sent, so assume lost
                system = system.functions.lostTransfer(data.details)
            }

            TransferData.Status.Created,
            TransferData.Status.Requested -> {
                // Aborted before sent, so assume ignored
                system = system.functions.ignore(mk_Transfer(data.details))
            }

            else -> throw IllegalStateException()
        }
        updateStatus(transferName, TransferData.Status.Aborted)
    }

}

class AbstractWorldAnimationBuilder(private val declarations: List<Declaration>) {

    fun build(): AbstractWorldAnimation {
        val worldDeclarations = declarations.filterIsInstance<WorldDeclaration>()
        require(worldDeclarations.size <= 1)
        val purses = mapping(declarations.filterIsInstance<PurseDeclaration>()) { dec ->
            mk_(dec.name.toName(), mk_AbPurse(dec.balance.toULong(), dec.lost.toULong()))
        }
        return AbstractWorldAnimation(
            system = mk_AbSystem(mk_AbWorld(purses)),
            transfers = declarations.filterIsInstance<TransferDeclaration>().associate { dec ->
                dec.name to TransferData(dec.name, dec.from, dec.to, dec.amount, TransferData.Status.Created)
            }.toMutableMap(),
            worldName = worldDeclarations.singleOrNull()?.name ?: "DEFAULT"
        )
    }

}

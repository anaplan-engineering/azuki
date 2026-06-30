package com.anaplan.engineering.azuki.mondex.adapter.kazuki

import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.ConPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Status
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbPurse_Module.mk_AbPurse
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbWorld
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbWorld_Module.mk_AbWorld
import com.anaplan.engineering.azuki.mondex.kazuki.betw.ConPurse_Module.mk_ConPurse
import com.anaplan.engineering.azuki.mondex.kazuki.betw.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.kazuki.core.*
import kotlin.collections.component1
import kotlin.collections.component2

class EnvironmentBuilder {

    private val declarations = LinkedHashMap<String, (ExecutionEnvironment) -> Any>()

    fun <T : Any> declare(name: String, declaration: (ExecutionEnvironment) -> T) {
        if (name in declarations.keys) {
            throw IllegalArgumentException("$name declared more than once")
        }
        declarations[name] = declaration
    }

    fun build(): ExecutionEnvironment {
        val env = ExecutionEnvironment()
        declarations.forEach { (n, fn) -> env.set(n, fn(env)) }
        return env
    }
}

class ExecutionEnvironment {

    val variables = mutableMapOf<String, Any>()

    inline fun <reified T> get(name: String) = variables[name] as T

    inline fun <reified T : Any> set(name: String, value: T) {
        variables[name] = value
    }

    // Thiss needs to handle both kinds of worlds
    internal fun world(name: String) = get<AbWorld>(name)
}

//LF @EK here we have a choice (good to ask AP/SF): either we accept an input with multiple or single kind of purse
fun buildWorld(
    purses: Map<String, AbPurse>,
): AbWorld {
    val world = mk_AbWorld(
    mapping(purses.entries) { (name, purse) -> mk_(name, mk_AbPurse(purse.balance, purse.lost))
        }
    )
    return world
}

fun Status.toKazuki() = when (this) {
    Status.eaFrom -> com.anaplan.engineering.azuki.mondex.kazuki.betw.Status.eaFrom
    Status.eaTo -> com.anaplan.engineering.azuki.mondex.kazuki.betw.Status.eaTo
    Status.epr -> com.anaplan.engineering.azuki.mondex.kazuki.betw.Status.epr
    Status.epv -> com.anaplan.engineering.azuki.mondex.kazuki.betw.Status.epv
    Status.epa -> com.anaplan.engineering.azuki.mondex.kazuki.betw.Status.epa
}
fun Set<PayDetails>.toKazuki() = map { it.toKazuki() }.toSet()
fun PayDetails.toKazuki() =
    mk_PayDetails(td.fromPurse, td.toPurse, td.value, fromSeqNo, toSeqNo)

// This needs to cater for both types of purse
// This should be the adapter's purse input, not Kazuki's, then  create corresponding
fun AbWorld.withPurse(
    purseName: String,
    purse: AbPurse,
) : AbWorld {
    val world = mk_AbWorld(
        abAuthPurse * mk_Mapping(mk_(purseName,
            //when(purse) {
            //is AbPurse ->
    mk_AbPurse(purse.balance, purse.lost)
//            is ConPurse -> mk_ConPurse(purse.balance, purse.exLog.toKazuki(),
//                purse.name, purse.nextSeqNo, purse.pdAuth?.toKazuki(),
//                purse.status.toKazuki())
//            //LF @QST given adapter-api's Purse is a sealed interface, why is this needed?
//            else -> throw IllegalArgumentException("Purse must be of type AbPurse or ConPurse")
//        }
                ))
    )
    return world
}

//LF @EK not sure this is the best/right way, but keeping it for now; simpler than when(purse) etc.
fun Map<String, AbPurse>.toAbMapping() =
    mapping(entries) { (name, purse) -> mk_(name, purse.toKazuki()) }

fun Map<String, ConPurse>.toConMapping() =
    mapping(entries) { (name, purse) -> mk_(name, purse.toKazuki()) }

fun AbPurse.toKazuki() = mk_AbPurse(balance, lost)
fun ConPurse.toKazuki() = mk_ConPurse(balance, exLog.toKazuki(), name, nextSeqNo, pdAuth?.toKazuki(), status.toKazuki())

package com.anaplan.engineering.azuki.mondex.adapter.api

sealed class World (
    //LF QST: maybe have this here?
    // protected val name: Name,
    protected val purses: Map<String, Purse>
)

class AbWorld(
    // narrow the type
    purses: Map<Name, AbPurse> = HashMap<Name, AbPurse>()
) : World(purses) {
    //LF QST: for refinement, maybe allow a map here with both purses within and just filter?
    // relates to Z's AbWorld.abAuthPurses
    @Suppress("UNCHECKED_CAST")
    val abAuthPurses: Map<Name, AbPurse> = purses as Map<Name, AbPurse>
    //val abAuthPurseAlt: Map<Name, AbPurse> = purses.filterValues { it is AbPurse }.mapValues { it.value as AbPurse }
}

interface CounterPartyDetails
sealed interface Clear
//LF: Injective projection for Clear over a non-empty set of pay details
class image(val pd1: Set/*Set1*/<PayDetails>): Clear

sealed class Message {
    data class StartFrom(val cpd: CounterPartyDetails) : Message()
    data class StartTo(val cpd: CounterPartyDetails) : Message()
    data object ReadExceptionLog : Message()
    data class Req(val pd: PayDetails) : Message()
    data class Val(val pd: PayDetails) : Message()
    data class Ack(val pd: PayDetails) : Message()
    data class ExceptionLogResult(val name: Name, val pd: PayDetails) : Message()
    data class ExceptionLogClear(val name: Name, val clear: Clear) : Message()
    data object Bottom : Message()
}

//LF QST: which one of these two are better? Got second version and found the first on
//    https://github.com/MarcinMoskala/KotlinDiscreteMathToolkit/blob/master/src/main/java/com/marcinmoskala/math/PowersetExt.kt
fun <T> Collection<T>.powerset(): Set<Set<T>> = powerset(this, setOf(setOf()))

private tailrec fun <T> powerset(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> = when {
    left.isEmpty() -> acc
    else ->powerset(left.drop(1), acc + acc.map { it + left.first() })
}

fun <T> Collection<T>.powerset2(): Set<Set<T>> =
    (this as? List<T> ?: toList()).fold(listOf(emptyList<T>())) { acc, e ->
        acc.flatMap { subset -> listOf(subset, subset + e) }
    }.map { it.toSet() }.toSet()

// we need powerset though: careful with the Z peculiarity about sets as types
typealias LogBook = Set<Pair<Name, PayDetails>>

fun logbook(pds: Set<PayDetails>): Set<LogBook> =
    (pds.map { pd ->  pd.td.fromPurse to pd } +
        pds.map { pd -> pd.td.toPurse to pd })
        .toSet().powerset()

open class ConWorld(
    purses: Map<Name, ConPurse> = HashMap<Name, ConPurse>(),
    val ether: Set<Message>,
    val archive: LogBook
) : World(purses) {
    // relates to Z's ConWorld.conAuthPurses
    @Suppress("UNCHECKED_CAST")
    val conAuthPurses: Map<Name, ConPurse> = purses as Map<Name, ConPurse>
}

class AuxWorld(
    purses: Map<Name, ConPurse>,
    ether: Set<Message>,
    archive: Set<Pair<Name, PayDetails>>// subset LogBook
) : ConWorld(purses, ether, archive) {
    // AuxWorld extra fields are properties of constructed ones

    val allLogs = archive + conAuthPurses.values.flatMap { it.exLog }
    //LF QST needs discussion, I don't think this is right, given the Z says all PayDetails possible, not just those in conAuthPurse
    val authenticFrom = conAuthPurses.values
        // only consider non-null PDs
        .filter { it.pdAuth != null }
        // get the PDs where pd.from in dom conAuthPurse
        .filter { it.pdAuth!!.td.fromPurse == it.name }.toSet()
}

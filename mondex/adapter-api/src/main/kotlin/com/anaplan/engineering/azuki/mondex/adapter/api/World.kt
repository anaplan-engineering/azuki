package com.anaplan.engineering.azuki.mondex.adapter.api

import com.anaplan.engineering.azuki.core.runner.Log
import kotlin.collections.component1
import kotlin.collections.component2

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

//LF QST: faster because of no drop(1) + and working with lists is faster than sets?
fun <T> Collection<T>.powerset2(): Set<Set<T>> =
    (this as? List<T> ?: toList()).fold(listOf(emptyList<T>())) { acc, e ->
        acc.flatMap { subset -> listOf(subset, subset + e) }
    }.map { it.toSet() }.toSet()

fun <T> Collection<T>.powerset3(): Set<Set<T>> =
    (this as? List<T> ?: toList()).powersetAcc3()

private tailrec fun <T> List<T>.powersetAcc3(
    acc: List<List<T>> = listOf(emptyList()),
    index: Int = 0,
): Set<Set<T>> {
    if (index >= size) return acc.map { it.toSet() }.toSet()
    val e = this[index]
    return powersetAcc3(
        acc = acc.flatMap { s -> listOf(s, s + e) },
        index = index + 1,
    )
}

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

open class AuxWorld(
    purses: Map<Name, ConPurse>,
    ether: Set<Message>,
    archive: LogBook
) : ConWorld(purses, ether, archive) {
    // AuxWorld extra fields are properties of constructed ones
}

class BetweenWorld(
    purses: Map<Name, ConPurse>,
    ether: Set<Message>,
    archive: LogBook
) : AuxWorld(purses, ether, archive)

//LF QST Type-bound comprehension is not executable in VDM; needs sequence?
//   Z says all PayDetails possible, not just those in conAuthPurse
//     { pd | pd : PayDetail & pd.td.from in set dom conAuthPurse }

fun allSimpleULongs(): Sequence<ULong> = generateSequence(0uL) { it + 1uL }
fun allSimpleNames(): Sequence<Name> = allSimpleULongs().map { "name$it" }
fun allPayDetails(
    // This is not quite like the Z (e.g. generation is deterministic and progressive; in Z its non-deterministic)
    // This also is "slow" on later dimensions (e.g., won't create as diverse set of PayDetails examples as needed)
    fromNames: Sequence<Name> = allSimpleNames(),
    toNames: Sequence<Name> = allSimpleNames(),
    values: Sequence<ULong> = generateSequence(0uL) { it + 1uL },
    fromSeqNos: Sequence<ULong> = generateSequence(0uL) { it + 1uL },
    toSeqNos: Sequence<ULong> = generateSequence(0uL) { it + 1uL },
): Sequence<PayDetails> {
    return fromNames.flatMap { from ->
        toNames.flatMap { to ->
            if (from == to) emptySequence()
            else values.flatMap { value ->
                fromSeqNos.flatMap { fromSeq ->
                    toSeqNos.map { toSeq ->
                        PayDetails(
                            td = TransferDetails(from, to, value),
                            fromSeqNo = fromSeq,
                            toSeqNo = toSeq,
                        )
                    }
                }
            }
        }
    }
}

// AuxWorld invariants are projections over the ConWorld class properties

// Is this representing the Z? How to "use" this (take(10).toSet())
// This produces an "unfair" sequencing for certain dimentions.
fun AuxWorld.allLogs(): LogBook =
    archive + conAuthPurses.flatMap { (n, purse) -> purse.exLog.map { pd -> n to pd } }
fun AuxWorld.authenticFrom() = allPayDetails(fromNames = conAuthPurses.keys.asSequence())
fun AuxWorld.authenticTo() = allPayDetails(toNames = conAuthPurses.keys.asSequence())
fun AuxWorld.fromLogged() = authenticFrom().filter { pd -> (pd.td.fromPurse to pd) in allLogs() }
fun AuxWorld.toLogged() = authenticTo().filter { pd -> (pd.td.toPurse to pd) in allLogs() }
// Because it comes from authenticTo(), then conAuthPurses[pd.td.toPurse] should always be set nicely
// Z equiv: toInEpv = { pd | pd in authenticTo() & (conAuthPruses pd.toPurse).status = epv && (conAuthPurses pd.toPurse).pdAuth = pd }
fun AuxWorld.toInEpv() = authenticTo()
    .filter { pd -> conAuthPurses[pd.td.toPurse]?.status == Status.epv &&
        (conAuthPurses[pd.td.toPurse]?.pdAuth == pd)}
fun AuxWorld.toInEpayee(): Sequence<PayDetails> = TODO()
fun AuxWorld.fromInEpr(): Sequence<PayDetails> = TODO()
fun AuxWorld.fromInEpa(): Sequence<PayDetails> = TODO()
//.....

// BetweenWorld invariants are projections over the class properties + AuxWorld invariants

// This will be easier in Kazuki then here. Not sure we actually need all this here and only in Kazuki.
fun BetweenWorld.B1(): Boolean = TODO()
// ... until B16(): Boolean = TODO()....

////////////////////// Cursor helped with a fairer comprehension over lazy sequences
/** O(n) element access — fine for exploration; cache if you need speed. */
fun <T> Sequence<T>.nth(n: Int): T = drop(n).first()

/** All index tuples of length [dims] whose components sum to [sum]. */
fun tuplesWithSum(dims: Int, sum: Int): Sequence<List<Int>> = sequence {
    if (dims == 1) {
        yield(listOf(sum))
        return@sequence
    }
    for (head in 0..sum) {
        tuplesWithSum(dims - 1, sum - head).forEach { tail ->
            yield(listOf(head) + tail)
        }
    }
}
fun allPayDetailsFair(
    fromNames: Sequence<Name> = allSimpleNames(),
    toNames: Sequence<Name> = allSimpleNames(),
    values: Sequence<ULong> = allSimpleULongs(),
    fromSeqNos: Sequence<ULong> = allSimpleULongs(),
    toSeqNos: Sequence<ULong> = allSimpleULongs(),
): Sequence<PayDetails> = sequence {
    val froms = fromNames.toList()   // finite in your ConWorld case
    val seqs = listOf(toNames, values, fromSeqNos, toSeqNos)

    var sum = 0
    while (true) {
        for (fi in froms.indices) {
            tuplesWithSum(4, sum).forEach { (ti, vi, fsi, tsi) ->
                val from = froms[fi]
                val to = toNames.nth(ti)
                if (from != to) {
                    yield(
                        PayDetails(
                            td = TransferDetails(from, to, values.nth(vi)),
                            fromSeqNo = fromSeqNos.nth(fsi),
                            toSeqNo = toSeqNos.nth(tsi),
                        )
                    )
                }
            }
        }
        sum++
    }
}

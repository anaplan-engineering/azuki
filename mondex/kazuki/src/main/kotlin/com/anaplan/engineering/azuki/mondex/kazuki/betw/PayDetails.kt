package com.anaplan.engineering.azuki.mondex.kazuki.betw

import com.anaplan.engineering.azuki.mondex.kazuki.Name
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.betw.PayDetails_Module.mk_PayDetails
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.nat

@Module
interface PayDetails : TransferDetails {
    val fromSeqNo: nat
    val toSeqNo: nat

    @Invariant
    fun namesDistinct() = from != to
}

@Module
interface CounterPartyDetails {
    val name: Name
    val value: nat
    val nextSeqNo: nat
}

sealed interface Clear
//LF @QST Injective projection for Clear over a non-empty set of pay details
class image(val pd1: Set1<PayDetails>): Clear

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

//LF @QST Type-bound comprehension is not executable in VDM; needs sequence?
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
                        mk_PayDetails(from, to, value, fromSeq, toSeq)
                    }
                }
            }
        }
    }
}

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
                    yield(mk_PayDetails(from, to, values.nth(vi), fromSeqNos.nth(fsi), toSeqNos.nth(tsi))
                    )
                }
            }
        }
        sum++
    }
}

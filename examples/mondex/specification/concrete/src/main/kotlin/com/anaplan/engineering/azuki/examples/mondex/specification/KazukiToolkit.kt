package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.VFunction1
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.inter
import com.anaplan.engineering.kazuki.core.is_Mapping
import com.anaplan.engineering.kazuki.core.mk_Relation
import com.anaplan.engineering.kazuki.core.set

fun <D, R> is_InjectiveMapping(vararg maplets: Tuple2<D, R>) =
//LF @QST why type error here? Missing in is_InjectiveMapping in kazuki core
    //is_Mapping(maplets) &&
    mk_Relation(*maplets).let { it.dom.card == it.card } &&
        mk_Relation(*maplets).let { it.rng.card == it.card }

fun <D, R> is_InjectiveMapping(maplets: Iterable<Tuple2<D, R>>) =
    is_Mapping(maplets) && as_Relation(maplets).let { it.rng.card == it.card }

//LF @QST which one of these two are better? Got second version and found the first on
//    https://github.com/MarcinMoskala/KotlinDiscreteMathToolkit/blob/master/src/main/java/com/marcinmoskala/math/PowersetExt.kt
fun <T> Collection<T>.powerset(): Set<Set<T>> = powerset(this, setOf(setOf()))//mk_Set(mk_Set()))

private tailrec fun <T> powerset(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> = when {
    left.isEmpty() -> acc
    else -> powerset(left.drop(1), acc + acc.map { it + left.first() })
}

//LF @QST faster because of no drop(1) + and working with lists is faster than sets?
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

fun <T> Set<T>.isSubsetOf(isInSuperset: (T) -> Boolean): Boolean = all(isInSuperset)

//TODO LF SF - Sequence inherits Collection (not Relation)? Using VDM_Toolkit one instead; could do with Set.fold1?
/*
--@doc generalised disjointness of sets
--@todo use fold? allow for set of rather than seq of?
disjoint[@elem]: seq of (set of @elem) +> bool
disjoint(s) ==
    -- empty or singleton sequences are trivially disjoint
    (len s > 1)
    =>
    -- other sequences of sets are disjoint if they are pairwise disjoint to all higher indexes (e.g., slightlty more efficient than POST?)
    len s = card { i | i in set inds s & forall j in set inds s & j > i => s(i) inter s(j) = {} }
--post
 */
fun <T> Sequence<Set<T>>.pairwise_disjoint() =
    (len > 1UL) implies {
        len == set(inds) { i -> forall(inds) { j -> (j > i) implies { (this[i] inter this[j]).isEmpty() } } }.card
    }

//fun <T> Sequence<T>.isSubsetOf(isInSuperset: (T) -> Boolean): Boolean =
//    distinct().all(isInSuperset)

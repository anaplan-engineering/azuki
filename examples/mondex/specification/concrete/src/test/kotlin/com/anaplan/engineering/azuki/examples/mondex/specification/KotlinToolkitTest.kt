package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set
import org.junit.Test

class KotlinToolkitTest {

    @Test
    fun pairwiseD() {
        assert(mk_Seq(
            mk_Set(1,2,3),
            mk_Set(4,5,6),
            mk_Set(7,8,9))
            .pairwise_disjoint())

        assert(!mk_Seq(
            mk_Set(1,2,3,4),
            mk_Set(4,5,6),
            mk_Set(7,8,9))
            .pairwise_disjoint())
    }

    @Test
    fun pairwiseD2() {
        assert(mk_Seq(
            mk_Set(1,2,3),
            mk_Set(4,5,6),
            mk_Set(7,8,9))
            .pairwise_disjoint_k())

        assert(!mk_Seq(
            mk_Set(1,2,3,4),
            mk_Set(4,5,6),
            mk_Set(7,8,9))
            .pairwise_disjoint_k())
    }
}

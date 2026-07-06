package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.kazuki.core.PostconditionFailure
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.VFunction1
import com.anaplan.engineering.kazuki.core.function
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * World-only counterpart to [KazukiCompositionTest]: [WorldCompStep] composes on [ToyWorld] directly,
 * without a System wrapper or system-to-host projection.
 */
class KazukiWorldCompTest {

    private data class ToyWorld(val trace: String = "")

    private data class ToyHost(
        val world: ToyWorld,
        val frames: WorldStepFrameStack<ToyWorld> = WorldStepFrameStack(),
    ) {
        fun atWorld(w: ToyWorld) = copy(world = w)
    }

    private object ToyGo : Message
    private object ToyStop : Message

    private val toyContext = WorldCompContext<ToyWorld, ToyHost>(
        worldOfHost = { it.world },
        hostAtWorld = { host, w -> host.atWorld(w) },
        frameStackOfHost = { it.frames },
    )

    private fun traceStep(tag: String, postCheck: (before: ToyWorld, middle: ToyWorld) -> Boolean) =
        worldCompStep<ToyWorld, ToyHost>(
            command = { _, _ -> { w -> w.copy(trace = w.trace + tag) } },
            post = { host, _, middle -> postCheck(host.world, middle) },
        )

    private fun stepA() = traceStep("A") { before, middle ->
        middle.trace == before.trace + "A"
    }

    private fun stepB() = traceStep("B") { before, middle ->
        middle.trace == before.trace + "B"
    }

    private fun stepC() = worldCompStep<ToyWorld, ToyHost>(
        command = { _, _ -> { w -> w.copy(trace = w.trace + "C") } },
        pre = { _, m -> m !== ToyStop },
        post = { host, _, result -> result.trace == host.world.trace + "C" },
    )

    private fun chainedOp() = stepA() compose stepB() using toyContext compose stepC() using toyContext

    private fun ToyHost.materialise(step: WorldCompStep<ToyWorld, ToyHost>): VFunction1<Message, ToyWorld> =
        worldComp(step, toyContext) { transform -> transform(world) }

    // --- broken variants for negative tests ---

    private class SingleSlotFrame<W> {
        var slot: W? = null
        fun pushMiddle(m: W) {
            slot = m
        }
        fun popMiddle(): W? = slot.also { slot = null }
    }

    private class SingleSlotFrameStack<W> {
        private val frames = ArrayDeque<SingleSlotFrame<W>>()
        fun push() = SingleSlotFrame<W>().also { frames.addLast(it) }
        fun pop() {
            frames.removeLastOrNull()
        }
        fun current() = frames.lastOrNull()
        fun frameDepth() = frames.size
    }

    private fun composeWithSingleMiddleSlot(
        left: WorldCompStep<ToyWorld, ToyHost>,
        right: WorldCompStep<ToyWorld, ToyHost>,
        slots: (ToyHost) -> SingleSlotFrameStack<ToyWorld>,
    ): WorldCompStep<ToyWorld, ToyHost> = worldCompStep(
        command = { host, m -> left.command(host, m) then right.command(host, m) },
        pre = { host, m ->
            if (!left.pre(host, m)) return@worldCompStep false
            val middle = middleWorld(host, left, m, toyContext)
            slots(host).current()!!.pushMiddle(middle)
            right.pre(host.atWorld(middle), m)
        },
        post = { host, m, result ->
            val middle = slots(host).current()!!.popMiddle()
                ?: middleWorld(host, left, m, toyContext)
            left.post(host, m, middle) && right.post(host.atWorld(middle), m, result)
        },
    )

    private fun opWithSingleMiddleSlot(
        slots: (ToyHost) -> SingleSlotFrameStack<ToyWorld>,
    ): WorldCompStep<ToyWorld, ToyHost> {
        val ab = composeWithSingleMiddleSlot(stepA(), stepB(), slots)
        return worldCompStep(
            command = { host, m -> ab.command(host, m) then stepC().command(host, m) },
            pre = { host, m ->
                if (!ab.pre(host, m)) return@worldCompStep false
                val middleAb = middleWorld(host, ab, m, toyContext)
                slots(host).current()!!.pushMiddle(middleAb)
                stepC().pre(host, m)
            },
            post = { host, m, result ->
                val middleAb = slots(host).current()!!.popMiddle()
                    ?: middleWorld(host, ab, m, toyContext)
                ab.post(host, m, middleAb) && stepC().post(host, m, result)
            },
        )
    }

    private fun worldCompWithSingleSlotStack(
        host: ToyHost,
        step: WorldCompStep<ToyWorld, ToyHost>,
        slots: SingleSlotFrameStack<ToyWorld>,
    ): VFunction1<Message, ToyWorld> = function(
        command = { m -> step.command(host, m)(host.world) },
        pre = { m ->
            slots.push()
            step.pre(host, m).also { ok -> if (!ok) slots.pop() }
        },
        post = { m, dash ->
            try {
                step.post(host, m, dash)
            } finally {
                slots.pop()
            }
        },
    )

    private fun worldCompWithoutPreFailDiscard(
        host: ToyHost,
        step: WorldCompStep<ToyWorld, ToyHost>,
    ): VFunction1<Message, ToyWorld> = function(
        command = { m -> step.command(host, m)(host.world) },
        pre = { m ->
            host.frames.push()
            step.pre(host, m) // bug: no pop when pre returns false
        },
        post = { m, dash ->
            try {
                step.post(host, m, dash)
            } finally {
                host.frames.pop()
            }
        },
    )

    // --- positive tests ---

    @Test
    fun innerStack_chainedCompose_success() {
        val host = ToyHost(ToyWorld())
        val fn = host.materialise(chainedOp())

        val result = fn(ToyGo)

        assertEquals("ABC", result.trace)
        assertEquals(0, host.frames.size)
    }

    @Test
    fun infixCompose_chainedWithAndWithoutParentheses_sameResult() {
        val host = ToyHost(ToyWorld())

        val withoutParens = stepA() compose stepB() using toyContext compose stepC() using toyContext
        val withParens = (stepA() compose stepB() using toyContext) compose stepC() using toyContext

        val resultWithoutParens = host.materialise(withoutParens)(ToyGo)
        val resultWithParens = host.materialise(withParens)(ToyGo)

        assertEquals("ABC", resultWithoutParens.trace)
        assertEquals(resultWithoutParens, resultWithParens)
        assertEquals(0, host.frames.size)
    }

    @Test
    fun outerStack_failedPreDiscardsFrame_retrySucceeds() {
        val host = ToyHost(ToyWorld())
        val fn = host.materialise(chainedOp())

        assertFailsWith<PreconditionFailure> { fn(ToyStop) }
        assertNull(host.frames.current(), "failed pre must discard the outer frame")

        val result = fn(ToyGo)
        assertEquals("ABC", result.trace)
        assertEquals(0, host.frames.size)
    }

    // --- negative tests ---

    @Test
    fun innerStack_singleSlotMiddle_postFailsOnChainedCompose() {
        val host = ToyHost(ToyWorld())
        val slots = SingleSlotFrameStack<ToyWorld>()
        val op = opWithSingleMiddleSlot { slots }
        val fn = worldCompWithSingleSlotStack(host, op, slots)

        // Inner (A ; B) caches W_A, then outer pre overwrites the single slot with W_AB.
        // (A ; B).post passes W_AB to A.post, which expects trace "...A" not "...AB".
        assertFailsWith<PostconditionFailure> { fn(ToyGo) }
        assertEquals(0, slots.frameDepth())
    }

    @Test
    fun outerStack_preFailWithoutDiscard_leavesStaleMiddlesInFrame() {
        val host = ToyHost(ToyWorld())
        val fn = worldCompWithoutPreFailDiscard(host, chainedOp())

        assertFailsWith<PreconditionFailure> { fn(ToyStop) }

        assertEquals(1, host.frames.size, "outer frame leaked when pre fails")
        assertEquals(
            2,
            host.frames.current()!!.size,
            "simulated W_A and W_AB middles must not survive a failed pre",
        )
    }
}

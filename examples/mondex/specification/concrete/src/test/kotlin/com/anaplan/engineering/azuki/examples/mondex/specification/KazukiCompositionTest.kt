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

class KazukiCompositionTest {

    private data class ToyWorld(val trace: String = "")

    private data class ToySystem(val world: ToyWorld)

    private class ToyFunctions(val system: ToySystem) {
        val worldStepFrames = WorldStepFrameStack<ToySystem>()
        fun update(transform: (ToyWorld) -> ToyWorld): ToySystem =
            ToySystem(transform(system.world))
    }

    private object ToyGo : Message
    private object ToyStop : Message

    private fun toyContext(frameStack: (ToyFunctions) -> WorldStepFrameStack<ToySystem>) =
        WorldStepHostContext(
            worldOfHost = { it.system.world },
            systemOfWorld = { ToySystem(it) },
            functionsOfSystem = { ToyFunctions(it) },
            frameStackOfHost = frameStack,
        )

    private fun traceStep(tag: String, postCheck: (before: ToyWorld, middle: ToyWorld) -> Boolean) =
        worldStep<ToySystem, ToyWorld, ToyFunctions, Message>(
            command = { _, _ -> { w -> w.copy(trace = w.trace + tag) } },
            post = { host, _, middle ->
                postCheck(host.system.world, middle.world)
            },
        )

    /** A.post: middle must be exactly one [tag] ahead of before (not two tags). */
    private fun stepA() = traceStep("A") { before, middle ->
        middle.trace == before.trace + "A"
    }

    private fun stepB() = traceStep("B") { before, middle ->
        middle.trace == before.trace + "B"
    }

    /** C.pre rejects [ToyStop]; post checks result extends host world by one C. */
    private fun stepC() = worldStep<ToySystem, ToyWorld, ToyFunctions, Message>(
        command = { _, _ -> { w -> w.copy(trace = w.trace + "C") } },
        pre = { _, m -> m !== ToyStop },
        post = { host, _, result ->
            result.world.trace == host.system.world.trace + "C"
        },
    )

    private fun chainedOp(ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>) =
        stepA() compose stepB() using ctx compose stepC() using ctx

    private fun ToyFunctions.materialise(
        step: WorldStep<ToySystem, ToyWorld, ToyFunctions, Message>,
        ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>,
    ): VFunction1<Message, ToySystem> = worldCompose(step, ctx) { transform -> update(transform) }

    // --- broken variants for negative tests ---

    /** Inner stack broken: one middle slot per frame — second push overwrites the first. */
    private class SingleSlotFrame<S> {
        var slot: S? = null
        fun pushMiddle(m: S) {
            slot = m
        }
        fun popMiddle(): S? = slot.also { slot = null }
    }

    private class SingleSlotFrameStack<S> {
        private val frames = ArrayDeque<SingleSlotFrame<S>>()
        fun push() = SingleSlotFrame<S>().also { frames.addLast(it) }
        fun pop() {
            frames.removeLastOrNull()
        }
        fun current() = frames.lastOrNull()
        fun frameDepth() = frames.size
    }

    private fun composeWithSingleMiddleSlot(
        left: WorldStep<ToySystem, ToyWorld, ToyFunctions, Message>,
        right: WorldStep<ToySystem, ToyWorld, ToyFunctions, Message>,
        ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>,
        slots: (ToyFunctions) -> SingleSlotFrameStack<ToySystem>,
    ): WorldStep<ToySystem, ToyWorld, ToyFunctions, Message> = worldStep(
        command = { host, m -> left.command(host, m) then right.command(host, m) },
        pre = { host, m ->
            if (!left.pre(host, m)) return@worldStep false
            val middle = middleSystem(host, left, m, ctx)
            slots(host).current()!!.pushMiddle(middle)
            right.pre(ctx.functionsOfSystem(middle), m)
        },
        post = { host, m, result ->
            val middle = slots(host).current()!!.popMiddle()
                ?: middleSystem(host, left, m, ctx)
            left.post(host, m, middle) && right.post(ctx.functionsOfSystem(middle), m, result)
        },
    )

    private fun opWithSingleMiddleSlot(
        ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>,
        slots: (ToyFunctions) -> SingleSlotFrameStack<ToySystem>,
    ): WorldStep<ToySystem, ToyWorld, ToyFunctions, Message> {
        val ab = composeWithSingleMiddleSlot(stepA(), stepB(), ctx, slots)
        return worldStep(
            command = { host, m -> ab.command(host, m) then stepC().command(host, m) },
            pre = { host, m ->
                if (!ab.pre(host, m)) return@worldStep false
                val middleAb = middleSystem(host, ab, m, ctx)
                slots(host).current()!!.pushMiddle(middleAb)
                stepC().pre(host, m)
            },
            post = { host, m, result ->
                val middleAb = slots(host).current()!!.popMiddle()
                    ?: middleSystem(host, ab, m, ctx)
                ab.post(host, m, middleAb) && stepC().post(host, m, result)
            },
        )
    }

    private fun worldComposeWithSingleSlotStack(
        host: ToyFunctions,
        step: WorldStep<ToySystem, ToyWorld, ToyFunctions, Message>,
        ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>,
        slots: SingleSlotFrameStack<ToySystem>,
    ): VFunction1<Message, ToySystem> = function(
        command = { m -> host.update(step.command(host, m)) },
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

    private fun worldComposeWithoutPreFailDiscard(
        host: ToyFunctions,
        step: WorldStep<ToySystem, ToyWorld, ToyFunctions, Message>,
        ctx: WorldStepHostContext<ToySystem, ToyWorld, ToyFunctions>,
    ): VFunction1<Message, ToySystem> = function(
        command = { m -> host.update(step.command(host, m)) },
        pre = { m ->
            ctx.frameStackOfHost(host).push()
            step.pre(host, m) // bug: no pop when pre returns false
        },
        post = { m, dash ->
            try {
                step.post(host, m, dash)
            } finally {
                ctx.frameStackOfHost(host).pop()
            }
        },
    )

    // --- positive tests ---

    @Test
    fun innerStack_chainedCompose_success() {
        val host = ToyFunctions(ToySystem(ToyWorld()))
        val ctx = toyContext { it.worldStepFrames }
        val fn = host.materialise(chainedOp(ctx), ctx)

        val result = fn(ToyGo)

        assertEquals("ABC", result.world.trace)
        assertEquals(0, host.worldStepFrames.size)
    }

    @Test
    fun infixCompose_chainedWithAndWithoutParentheses_sameResult() {
        val host = ToyFunctions(ToySystem(ToyWorld()))
        val ctx = toyContext { it.worldStepFrames }

        val withoutParens = stepA() compose stepB() using ctx compose stepC() using ctx
        val withParens = (stepA() compose stepB() using ctx) compose stepC() using ctx

        val resultWithoutParens = host.materialise(withoutParens, ctx)(ToyGo)
        val resultWithParens = host.materialise(withParens, ctx)(ToyGo)

        assertEquals("ABC", resultWithoutParens.world.trace)
        assertEquals(resultWithoutParens, resultWithParens)
        assertEquals(0, host.worldStepFrames.size)
    }

    @Test
    fun outerStack_failedPreDiscardsFrame_retrySucceeds() {
        val host = ToyFunctions(ToySystem(ToyWorld()))
        val ctx = toyContext { it.worldStepFrames }
        val fn = host.materialise(chainedOp(ctx), ctx)

        assertFailsWith<PreconditionFailure> { fn(ToyStop) }
        assertNull(host.worldStepFrames.current(), "failed pre must discard the outer frame")

        val result = fn(ToyGo)
        assertEquals("ABC", result.world.trace)
        assertEquals(0, host.worldStepFrames.size)
    }

    // --- negative tests ---

    @Test
    fun innerStack_singleSlotMiddle_postFailsOnChainedCompose() {
        val host = ToyFunctions(ToySystem(ToyWorld()))
        val ctx = toyContext { it.worldStepFrames }
        val slots = SingleSlotFrameStack<ToySystem>()
        val op = opWithSingleMiddleSlot(ctx) { slots }
        val fn = worldComposeWithSingleSlotStack(host, op, ctx, slots)

        // Inner (A ; B) caches S_A, then outer pre overwrites the single slot with S_AB.
        // (A ; B).post passes S_AB to A.post, which expects trace "...A" not "...AB".
        assertFailsWith<PostconditionFailure> { fn(ToyGo) }
        assertEquals(0, slots.frameDepth())
    }

    @Test
    fun outerStack_preFailWithoutDiscard_leavesStaleMiddlesInFrame() {
        val host = ToyFunctions(ToySystem(ToyWorld()))
        val ctx = toyContext { it.worldStepFrames }
        val fn = worldComposeWithoutPreFailDiscard(host, chainedOp(ctx), ctx)

        assertFailsWith<PreconditionFailure> { fn(ToyStop) }

        assertEquals(1, host.worldStepFrames.size, "outer frame leaked when pre fails")
        assertEquals(
            2,
            host.worldStepFrames.current()!!.size,
            "simulated S_A and S_AB middles must not survive a failed pre",
        )
    }
}

package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.kazuki.core.VFunction1
import com.anaplan.engineering.kazuki.core.function

// World-only variant of [KazukiComposition]: compose on W directly, without S / system projection.
class WorldCompContext<W, F>(
    val worldOfHost: (F) -> W,
    val hostAtWorld: (F, W) -> F,
    val frameStackOfHost: (F) -> WorldStepFrameStack<W>,
)

class WorldCompStep<W, F>(
    val command: (F, Message) -> (W) -> W,
    val pre: (F, Message) -> Boolean,
    val post: (F, Message, W) -> Boolean,
)

fun <W, F> worldCompStep(
    command: (F, Message) -> (W) -> W,
    pre: (F, Message) -> Boolean = { _, _ -> true },
    post: (F, Message, W) -> Boolean = { _, _, _ -> true },
): WorldCompStep<W, F> = WorldCompStep(command, pre, post)

fun <W, F> middleWorld(
    host: F,
    step: WorldCompStep<W, F>,
    m: Message,
    ctx: WorldCompContext<W, F>,
): W = ctx.worldOfHost(host).let(step.command(host, m))

// Picks current frame, which must exist
private fun <W, F> WorldCompContext<W, F>.currentFrame(host: F): WorldStepFrame<W> =
    frameStackOfHost(host).current()
        ?: error("worldComp must push a WorldStepFrame before step pre/post")

// Returns earlier computed middle, or creates the first one
private fun <W, F> WorldCompContext<W, F>.cachedMiddle(
    host: F,
    left: WorldCompStep<W, F>,
    m: Message,
): W = currentFrame(host).popMiddle() ?: middleWorld(host, left, m, this)

class WorldCompStepCompose<W, F>(
    private val left: WorldCompStep<W, F>,
    private val right: WorldCompStep<W, F>,
) {
    infix fun using(ctx: WorldCompContext<W, F>): WorldCompStep<W, F> =
        left.composeWith(right, ctx)
}

infix fun <W, F> WorldCompStep<W, F>.compose(
    right: WorldCompStep<W, F>,
): WorldCompStepCompose<W, F> = WorldCompStepCompose(this, right)

fun <W, F> WorldCompStep<W, F>.composeWith(
    right: WorldCompStep<W, F>,
    ctx: WorldCompContext<W, F>,
): WorldCompStep<W, F> {
    val left = this
    return worldCompStep(
        command = { host, m -> left.command(host, m) then right.command(host, m) },
        // pre[(P ; Q)(w)] = let m = exec(P, w) in pre[P(w)] && pre[Q(m)]
        pre = { host, m ->
            left.pre(host, m) && middleWorld(host, left, m, ctx).let { middle ->
                ctx.currentFrame(host).pushMiddle(middle)
                right.pre(ctx.hostAtWorld(host, middle), m)
            }
//            // compose pre with guard? Nah
//            guard(left.pre(host, m)) {
//                val middle = middleWorld(host, left, m, ctx)
//                ctx.currentFrame(host).pushMiddle(middle)
//                right.pre(ctx.hostAtWorld(host, middle), m)
//            }
        },
        // post[(P ; Q)(w)] = let m = exec(P, w), r = exec(Q, m) in post[P(w), m] && post[Q(m), r]
        post = { host, m, result ->
            ctx.cachedMiddle(host, left, m).let { middle ->
                left.post(host, m, middle) &&
                    right.post(ctx.hostAtWorld(host, middle), m, result)
            }
        },
    )
}

private inline fun guard(pre: Boolean, block: () -> Boolean): Boolean = pre && block()

// Top-level world step composition
fun <W, F> F.worldComp(
    step: WorldCompStep<W, F>,
    ctx: WorldCompContext<W, F>,
    updateWorld: F.((W) -> W) -> W,
): VFunction1<Message, W> = function(
    command = { m -> updateWorld(step.command(this, m)) },
    pre = { m ->
        val stack = ctx.frameStackOfHost(this)
        stack.push()
        step.pre(this, m).also { ok ->
            if (!ok) stack.pop()
        }
    },
    post = { m, dash ->
        try {
            step.post(this, m, dash)
        } finally {
            ctx.frameStackOfHost(this).pop()
        }
    },
)

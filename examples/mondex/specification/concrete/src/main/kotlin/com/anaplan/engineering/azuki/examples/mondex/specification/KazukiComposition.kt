package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.kazuki.core.VFunction1
import com.anaplan.engineering.kazuki.core.function

// World forward composition: (f ; g)(W) = g(f(W)), shame `;` not possible
infix fun <W> ((W) -> W).then(next: (W) -> W): (W) -> W = { w -> next(this(w)) }
//infix fun <W> ((W) -> W).`;`(next: (W) -> W): (W) -> W = then(next)

// Conversions between system, world, and function-provider host for [WorldStep] composition.
// e.g. how to get world out of FP (fp.system.conWorld), how to get world out of system etc.
class WorldStepHostContext<S, W, F>(
    val worldOfHost: (F) -> W,                          // host fp -> world before state
    val systemOfWorld: (W) -> S,                        // world after state -> system
    val functionsOfSystem: (S) -> F,                    // middle system -> host fp next call
    val frameStackOfHost: (F) -> WorldStepFrameStack<S>,// middle layer stack for chained / reentrant pre/post
                        // e.g. (A compose B) compose C, and when A.pre calls chained pre/post
)

// Middle world for one  [worldCompose] invocation (pre through post).
// e.g. which compose level’s middle am I caching? [i.e. important for chains (A compose B) compose C]
class WorldStepFrame<S> {
    //TODO LF - this could be a map in case of more complicated caches?
    /*
    class WorldStepFrame<S> { val middles = mutableMapOf<WorldStepMiddleKey, S>() }
    data class WorldStepMiddleKey(val stepId: Any, val input: Any)
    */
    private val middles = ArrayDeque<S>()
    val size get() = middles.size

    fun pushMiddle(middle: S) = middles.addLast(middle)
    fun popMiddle(): S? = middles.removeLastOrNull()
}

// Stack of invocation frames; supports re-entrant pre/post on the same host; scope boundary
// e.g. tracks which VFunction invocation am I in (i.e. reentrancy isolation, when you get Op1.pre calling Op2.post/pre)
class WorldStepFrameStack<S> {
    private val frames = ArrayDeque<WorldStepFrame<S>>()
    val size get() = frames.size

    fun push() = frames.addLast(WorldStepFrame())
    fun pop() { frames.removeLastOrNull() }
    fun current(): WorldStepFrame<S>? = frames.lastOrNull()
}

private fun <S, W, F> WorldStepHostContext<S, W, F>.currentFrame(host: F): WorldStepFrame<S> =
    frameStackOfHost(host).current() ?: error("worldCompose must push a WorldStepFrame before step pre/post")

private fun <S, W, F, I> WorldStepHostContext<S, W, F>.cachedMiddle(
    host: F,
    left: WorldStep<S, W, F, I>,
    input: I,
): S = currentFrame(host).popMiddle() ?: middleSystem(host, left, input, this)

//TODO LF - with KSP could identify all elements in a VFunction command?
//          1) collect a update frame (e.g. variables that changed, to create xiRest post);
//          2) collect function calls (e.g. if some flag is on, chain pre/posts implicitly for stricter checking)
//     Perhaps take inspiration from https://leanprover.github.io/functional_programming_in_lean/monads/class.html

class WorldStep<S, W, F, I>(
    val command: (F, I) -> (W) -> W,
    val pre: (F, I) -> Boolean,
    val post: (F, I, S) -> Boolean,
)

// Like Kazuki's function(...), but for host-parameterised world steps rather than VFunctions.
fun <S, W, F, I> worldStep(
    command: (F, I) -> (W) -> W,
    pre: (F, I) -> Boolean = { _, _ -> true },
    post: (F, I, S) -> Boolean = { _, _, _ -> true },
): WorldStep<S, W, F, I> = WorldStep(command, pre, post)

// Akin to Z \semi: P \semi Q \defs (\exists middle & P[middle/after] \land Q[middle/before])
fun <S, W, F, I> middleSystem(
    host: F,
    step: WorldStep<S, W, F, I>,
    input: I,
    ctx: WorldStepHostContext<S, W, F>,
): S = ctx.worldOfHost(host)
    .let { w -> step.command(host, input)(w) }
    .let { wMiddle -> ctx.systemOfWorld(wMiddle) }

// (f ; g) at step level: chains world/pre/post
class WorldStepCompose<S, W, F, I>(
    private val left: WorldStep<S, W, F, I>,
    private val right: WorldStep<S, W, F, I>,
) {
    infix fun using(ctx: WorldStepHostContext<S, W, F>): WorldStep<S, W, F, I> =
        left.composeWith(right, ctx)
}

infix fun <S, W, F, I> WorldStep<S, W, F, I>.compose(
    right: WorldStep<S, W, F, I>,
): WorldStepCompose<S, W, F, I> = WorldStepCompose(this, right)

fun <S, W, F, I> WorldStep<S, W, F, I>.composeWith(
    right: WorldStep<S, W, F, I>,
    ctx: WorldStepHostContext<S, W, F>,
): WorldStep<S, W, F, I> {
    val left = this
    return worldStep<S, W, F, I>(
        command = { host, input -> left.command(host, input) then right.command(host, input) },
        pre = { host, input ->
            left.pre(host, input) && middleSystem(host, left, input, ctx).let { middle ->
                ctx.currentFrame(host).pushMiddle(middle)
                right.pre(ctx.functionsOfSystem(middle), input)
            }
        },
        post = { host, input, result ->
            ctx.cachedMiddle(host, left, input).let { middle ->
                left.post(host, input, middle) && right.post(ctx.functionsOfSystem(middle), input, result)
            }
        },
    )
}

// Transforms the overall composition as a VFunction taking care of potential reentrant calls
fun <S, W, F, I> F.worldCompose(
    step: WorldStep<S, W, F, I>,
    ctx: WorldStepHostContext<S, W, F>,
    updateWorld: F.((W) -> W) -> S,
): VFunction1<I, S> = function(
    command = { input -> updateWorld(step.command(this, input)) },
    pre = { input ->
        val stack = ctx.frameStackOfHost(this)
        stack.push()
        step.pre(this, input).also { ok ->
            if (!ok) stack.pop()
        }
    },
    post = { input, dash ->
        try {
            step.post(this, input, dash)
        } finally {
            ctx.frameStackOfHost(this).pop()
        }
    },
)

package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace
import org.slf4j.LoggerFactory

// TODO LF: maybe for the kazuki link, no need for aircraft names?
abstract class AbstractCheck(
    private val airspaceName: String,
    private val aircraft0: String,
    private val aircraft1: String,
    override val behavior: Behavior,
) : ReifiedBehavior, KazukiCheck {

    final override fun check(env: ExecutionEnvironment): Boolean {
        return env.withAirspace(airspaceName) {
            val a0 = env.aircraft(airspaceName, aircraft0)
            val a1 = env.aircraft(airspaceName, aircraft1)
            Log.info("Checking airspace $airspaceName for $aircraft0 and $aircraft1 ${this@AbstractCheck::class.simpleName}")
                booleanCheck(a0.toKazuki(), a1.toKazuki())
        }
    }

    //TODO best way to allow for easy extension?
    protected abstract fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean

    protected val Log = LoggerFactory.getLogger(this::class.java)

}

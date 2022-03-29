package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.dsl.*
import com.anaplan.engineering.azuki.core.scenario.AbstractVerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.junit.runner.RunWith

@RunWith(JUnitScenarioRunner::class)
abstract class RunnableScenario<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    G : Given<AF>,
    W : When<AF>,
    T : Then<CF>,
    V : Verify<QF>,
    Q : Queries<QF>,
    R : Generate<AGF>,
    RO : RegardlessOf<AF>,
    SD : SystemDefaults,
    >(
    dslProvider: DslProvider<AF, CF, QF, AGF, G, W, T, V, Q, R, RO>
) : AbstractVerifiableScenario<AF, CF, G, W, T, RO>(dslProvider) {


}



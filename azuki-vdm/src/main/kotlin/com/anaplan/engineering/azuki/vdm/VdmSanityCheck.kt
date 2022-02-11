package com.anaplan.engineering.azuki.vdm

abstract class VdmSanityCheck<SC: SystemContext> : VdmCheck<SC> {

    override fun build(builder: ModuleBuilder<SC>): ModuleBuilder<SC> {
        return builder.extend(
            requiredImports = emptySet(),
            testSteps = listOf("skip;")
        )
    }

}

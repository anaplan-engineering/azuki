package com.anaplan.engineering.azuki.vdm

import com.anaplan.engineering.azuki.core.system.Check

interface VdmCheck<SC: SystemContext> : Check {

    fun build(builder: ModuleBuilder<SC>): ModuleBuilder<SC>

}


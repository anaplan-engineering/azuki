package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.VdmSanityCheck

object SystemValidCheck : VdmSanityCheck<EmptySystemContext>(), DefaultVdmCheck {

    override val behavior: Behavior get() = unsupportedBehavior
}

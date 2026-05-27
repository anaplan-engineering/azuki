package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.AbstractTransferOkayTD_Module.mk_AbstractTransferOkayTD
import com.anaplan.engineering.azuki.mondex.kazuki.Delta_Module.mk_Delta
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.mk_Purse
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.mk_World
import com.anaplan.engineering.kazuki.core.*
import org.junit.Test

class MondexTest {

    @Test
    fun newWorld() {
        val purse1 = mk_Purse(3UL,0UL)
        val purse2 = mk_Purse(1UL,0UL)

        val purse1Dash = mk_Purse(2UL, 0UL)
        val purse2Dash = mk_Purse(2UL, 0UL)

        val transferDetail = mk_TransferDetails("Erin", "Erin2", 1UL)

        val authPurseMapping = mk_Mapping(mk_("Erin", purse1), mk_("Erin2", purse2))
        val authPurseMappingDash = mk_Mapping(mk_("Erin", purse1Dash), mk_("Erin2", purse2Dash))

        val deltaWorld: Delta<World> = mk_Delta(mk_World(authPurseMapping), mk_World(authPurseMappingDash))

        val absOp = mk_AbstractTransferOkayTD(deltaWorld, transfer(transferDetail), aNullOut, transferDetail)
    }

}

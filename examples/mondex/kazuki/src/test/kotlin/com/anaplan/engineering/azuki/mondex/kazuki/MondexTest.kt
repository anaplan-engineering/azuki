package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbPurse_Module.mk_AbPurse
import com.anaplan.engineering.azuki.mondex.kazuki.abs.AbWorld_Module.mk_AbWorld
import com.anaplan.engineering.azuki.mondex.kazuki.abs.aNullIn
import com.anaplan.engineering.azuki.mondex.kazuki.abs.transfer
import com.anaplan.engineering.kazuki.core.*
import org.junit.Test
import kotlin.test.*

class MondexTest {

    @Test
    fun newWorld() {
        val purse1 = mk_AbPurse(3UL,0UL)
        val purse2 = mk_AbPurse(1UL,0UL)

        val world = mk_AbWorld(mk_Mapping(mk_("person1", purse1), mk_("person2", purse2)))

        val transferDetails = mk_TransferDetails("person1", "person2", 3UL)

        val ain = transfer(transferDetails)

        val (world2, _) = world.functions.abTransferOkayTD(ain, transferDetails)

        assertEquals(world2.abAuthPurse["person1"].balance, 0UL)
        assertEquals(world2.abAuthPurse["person2"].balance, 4UL)

        val (world3, _) = world.functions.abTransferLostTD(ain,transferDetails)

        assertEquals(world3.abAuthPurse["person1"].balance, 0UL)
        assertEquals(world3.abAuthPurse["person1"].lost, 3UL)

        // For Ignore, the initial message doesn't matter
        val (world4, _) = world.functions.abIgnore(aNullIn)

        assertEquals(world, world4)
        }
}

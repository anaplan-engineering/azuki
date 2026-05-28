package com.anaplan.engineering.azuki.mondex.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.mk_Purse
import com.anaplan.engineering.azuki.mondex.kazuki.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.mk_World
import com.anaplan.engineering.kazuki.core.*
import org.junit.Test
import kotlin.test.*

class MondexTest {

    @Test
    fun newWorld() {
        val purse1 = mk_Purse(3UL,0UL)
        val purse2 = mk_Purse(1UL,0UL)

        val world = mk_World(mk_Mapping(mk_("person1", purse1), mk_("person2", purse2)))

        val transferDetails = mk_TransferDetails("person1", "person2", 3UL)

        val world2 = world.functions.abstractTransferOkayTD(transfer(transferDetails), transferDetails)

        assertEquals(world2.authPurses["person1"].balance, 0UL)
        assertEquals(world2.authPurses["person2"].balance, 4UL)

        val world3 = world.functions.abstractTransferLostTD(transfer(transferDetails), transferDetails)

        assertEquals(world3.authPurses["person1"].balance, 0UL)
        assertEquals(world3.authPurses["person1"].lost, 3UL)

        val world4 = world.functions.abstractIgnore(transfer(transferDetails))

        assertEquals(world, world4)
        }
}

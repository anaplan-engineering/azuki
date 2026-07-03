package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbPurse_Module.mk_AbPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbWorld_Module.mk_AbWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.TransferDetails_Module.mk_TransferDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.Transfer_Module.mk_Transfer
import com.anaplan.engineering.kazuki.core.*
import org.junit.Test
import kotlin.test.assertEquals

class MondexTest {

    @Test
    fun newWorld() {
        val purse1 = mk_AbPurse(3UL, 0UL)
        val purse2 = mk_AbPurse(1UL, 0UL)

        val nameOfPerson1 = "person1".toName()
        val nameOfPerson2 = "person2".toName()

        val world = mk_AbWorld(mk_Mapping(mk_(nameOfPerson1, purse1), mk_(nameOfPerson2, purse2)))

        val transferDetails = mk_TransferDetails(nameOfPerson1, nameOfPerson2, 3UL)

        val ain = mk_Transfer(transferDetails)

        val (world2, _) = world.functions.abTransferOkayTD(ain, transferDetails)

        assertEquals(world2.abAuthPurse[nameOfPerson1].balance, 0UL)
        assertEquals(world2.abAuthPurse[nameOfPerson2].balance, 4UL)

        val (world3, _) = world.functions.abTransferLostTD(ain, transferDetails)

        assertEquals(world3.abAuthPurse[nameOfPerson1].balance, 0UL)
        assertEquals(world3.abAuthPurse[nameOfPerson1].lost, 3UL)

        // For Ignore, the initial message doesn't matter
        val (world4, _) = world.functions.abIgnore(aNullIn)

        assertEquals(world, world4)
    }
}

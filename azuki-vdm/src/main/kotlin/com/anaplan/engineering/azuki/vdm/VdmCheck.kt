package com.anaplan.engineering.azuki.vdm

import com.anaplan.engineering.azuki.core.system.Check

interface VdmCheck<SC: SystemContext> : Check {

    fun build(builder: ModuleBuilder<SC>): ModuleBuilder<SC>

    fun checkEquals(actual: String = "actual", expected: String = "expected", tolerant: Boolean = true, msg: String? = null) =
        """
            if not $actual = $expected
            then return false
            else skip;
        """
// can't rely on println being available at moment
//        """
//            if not $actual = $expected
//            then (
//                $vdmPrintln("* Check '${javaClass.simpleName}${if (msg == null) "" else ":$msg"}' failed (tolerant = $tolerant)");
//                $vdmPrint("    Expected: ");
//                $vdmPrintln($expected);
//                $vdmPrint("    Actual: ");
//                $vdmPrintln($actual);
//                return false;
//            )
//            else $vdmPrintln("* Check '${javaClass.simpleName}${if (msg == null) "" else ":$msg"}' passed");
//        """

}

typealias DefaultVdmCheck = VdmCheck<EmptySystemContext>

val toDefaultVdmCheck: (Check) -> DefaultVdmCheck = {
    @Suppress("UNCHECKED_CAST")
    it as? DefaultVdmCheck ?: throw IllegalArgumentException("Invalid check: $it")
}


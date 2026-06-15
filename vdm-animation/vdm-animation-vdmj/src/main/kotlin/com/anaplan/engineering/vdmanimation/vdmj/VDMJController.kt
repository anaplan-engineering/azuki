package com.anaplan.engineering.vdmanimation.vdmj

import com.fujitsu.vdmj.lex.Dialect
import java.io.File

class VDMJController(val dialect: Dialect = Dialect.VDM_SL, val showW: Boolean = true, val files: List<File> = emptyList()) {
//    Lifecycle(dialect, *.toTypedArray()){

    val toolLifeCycle: ToolLifeCycle by lazy {
        val tc = ToolLifeCycle(dialect.argstring, showW, files)
        tc.init()
        tc
    }
}

package com.anaplan.engineering.vdmanimation.overture

import com.anaplan.engineering.vdmanimation.api.AnimationCoverage
import com.anaplan.engineering.vdmanimation.api.FileCoverage
import com.anaplan.engineering.vdmanimation.api.Location
import org.overture.ast.intf.lex.ILexLocation
import org.overture.ast.lex.LexLocation
import org.overture.ast.modules.AModuleModules
import org.overture.interpreter.runtime.ModuleInterpreter
import java.io.File

class CoverageGenerator {

    fun generate(interpreter: ModuleInterpreter, vararg excludedModules: String) =
                generate(interpreter.modules, { file -> LexLocation.getSourceLocations(file) }, *excludedModules)

    internal fun generate(modules: List<AModuleModules>, locationGetter: (File) -> List<ILexLocation>, vararg excludedModules: String): AnimationCoverage {
        return AnimationCoverage(modules.filter { !excludedModules.contains(it.name.name) }.flatMap { module ->
            module.files.map { file ->
                val locationCoverage = locationGetter(file).map { location ->
                    Location(
                            location.startLine,
                            location.startPos,
                            location.endLine,
                            location.endPos
                    ) to location.hits
                }.toMap()
                FileCoverage(module.name.name, file.readText(), locationCoverage)
            }
        })
    }

}

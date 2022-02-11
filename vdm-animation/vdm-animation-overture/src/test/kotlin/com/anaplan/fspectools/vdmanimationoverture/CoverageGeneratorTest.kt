package com.anaplan.systemspecification.vdmanimationoverture

import com.anaplan.engineering.vdmanimation.api.Location
import com.anaplan.engineering.vdmanimation.overture.CoverageGenerator
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert
import org.junit.Test
import org.overture.ast.lex.LexLocation
import org.overture.ast.lex.LexNameToken
import org.overture.ast.modules.AModuleModules
import org.overture.ast.util.ClonableFile
import java.nio.file.Files

class CoverageGeneratorTest {

    @Test
    fun singleModule_SingleFile_NoExclusions_NoLocations() {
        val generator = CoverageGenerator()
        val module = mock<AModuleModules>()
        whenever(module.name).thenReturn(LexNameToken("", "name", null))
        // need a real file as we use the readText extension method
        val moduleFile = Files.createTempFile(javaClass.simpleName, ".vdmsl").toFile()
        moduleFile.writeText("ABCDEF")
        whenever(module.files).thenReturn(mutableListOf(ClonableFile(moduleFile)))
        val modules = listOf(module)
        val coverage = generator.generate(modules, { emptyList() })
        Assert.assertEquals(1, coverage.files.size)
        Assert.assertTrue(coverage.files.all { it.coverage.isEmpty() })
    }

    @Test
    fun singleModule_SingleFile_NoExclusions_WithLocations() {
        val generator = CoverageGenerator()
        val module = mock<AModuleModules>()
        val moduleNaem = "name"
        whenever(module.name).thenReturn(LexNameToken("", moduleNaem, null))
        // need a real file as we use the readText extension method
        val moduleFile = Files.createTempFile(javaClass.simpleName, ".vdmsl").toFile()
        moduleFile.writeText("ABCDEF")
        val file = ClonableFile(moduleFile)
        whenever(module.files).thenReturn(mutableListOf(file))
        val modules = listOf(module)
        val element = LexLocation(file, moduleNaem, 1, 1, 1, 3, 0, 0)
        element.hits = 3
        val element2 = LexLocation(file, moduleNaem, 1, 3, 1, 6, 0, 0)
        element2.hits = 0
        val coverage = generator.generate(modules, { listOf(element, element2) })
        Assert.assertEquals(1, coverage.files.size)
        coverage.files.forEach { fileCoverage ->
            Assert.assertEquals(setOf(
                    Location(1, 1, 1, 3),
                    Location(1, 3, 1, 6)
            ), fileCoverage.coverage.keys)
        }
    }

    @Test
    fun singleModule_MultipleFiles_NoExclusions() {
        val generator = CoverageGenerator()
        val module = mock<AModuleModules>()
        val moduleNaem = "name"
        whenever(module.name).thenReturn(LexNameToken("", moduleNaem, null))
        // need a real file as we use the readText extension method
        val moduleFile = Files.createTempFile(javaClass.simpleName, ".vdmsl").toFile()
        moduleFile.writeText("ABCDEF")
        val moduleFile2 = Files.createTempFile(javaClass.simpleName, ".vdmsl").toFile()
        moduleFile2.writeText("GHILKM")
        val file1 = ClonableFile(moduleFile)
        val file2 = ClonableFile(moduleFile2)
        whenever(module.files).thenReturn(mutableListOf(file1, file2))
        val modules = listOf(module)
        val element = LexLocation(file1, moduleNaem, 1, 1, 1, 3, 0, 0)
        element.hits = 3
        val element2 = LexLocation(file1, moduleNaem, 1, 3, 1, 6, 0, 0)
        element2.hits = 0
        val element3 = LexLocation(file2, moduleNaem, 1, 1, 1, 3, 0, 0)
        element3.hits = 3
        val element4 = LexLocation(file2, moduleNaem, 1, 3, 1, 6, 0, 0)
        element4.hits = 0
        val coverage = generator.generate(modules, {
            when (it) {
                file1 -> listOf(element, element2)
                file2 -> listOf(element3, element4)
                else -> throw IllegalArgumentException()
            }
        })
        Assert.assertEquals(2, coverage.files.size)
        coverage.files.forEach { fileCoverage ->
            when (fileCoverage.text) {
                "ABCDEF" -> {
                    Assert.assertEquals(setOf(
                            Location(1, 1, 1, 3),
                            Location(1, 3, 1, 6)
                    ), fileCoverage.coverage.keys)
                    Assert.assertEquals(mapOf(
                            Location(1, 1, 1, 3) to 3L,
                            Location(1, 3, 1, 6) to 0L
                    ), fileCoverage.coverage)

                }
                "GHILKM" -> Assert.assertEquals(setOf(
                        Location(1, 1, 1, 3),
                        Location(1, 3, 1, 6)
                ), fileCoverage.coverage.keys)
                else -> throw IllegalArgumentException()
            }
        }
    }

}

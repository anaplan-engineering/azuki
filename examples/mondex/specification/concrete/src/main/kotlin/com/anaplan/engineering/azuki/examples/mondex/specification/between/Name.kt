package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name_Module.mk_Name
import com.anaplan.engineering.kazuki.core.*

@Module
interface Name: Sequence<Char>

fun String.toName() = mk_Name(*toCharArray())

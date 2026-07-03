package com.anaplan.engineering.azuki.examples.mondex.specification.abstract

import com.anaplan.engineering.azuki.examples.mondex.specification.abstract.AbWorld_Module.mk_AbWorld
import com.anaplan.engineering.kazuki.core.*

val AbInitState = function<AbWorld>(command = { mk_AbWorld(mk_Mapping()) })

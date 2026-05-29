package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.core.system.Implementation

interface RightOfWayeImplementation :
    Implementation<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults>

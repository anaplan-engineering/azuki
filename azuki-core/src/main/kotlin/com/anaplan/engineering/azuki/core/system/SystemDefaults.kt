package com.anaplan.engineering.azuki.core.system

interface SystemDefaults

object NoSystemDefaults : SystemDefaults

interface SystemDefaultsResolver<SD : SystemDefaults> {
        val systemDefaults: SD

    fun resolve() = NoSystemDefaults
}

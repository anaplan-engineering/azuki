package com.anaplan.engineering.azuki.core.system

@Target(AnnotationTarget.CLASS)
annotation class BEH(val behavior: Behavior, val functionalElement: FunctionalElement, val summary: String)

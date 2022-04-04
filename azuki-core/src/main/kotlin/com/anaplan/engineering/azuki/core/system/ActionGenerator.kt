package com.anaplan.engineering.azuki.core.system

interface ActionGeneratorFactory

interface ActionGenerator

object NoActionGeneratorFactory : ActionGeneratorFactory

object UnsupportedActionGenerator : ActionGenerator

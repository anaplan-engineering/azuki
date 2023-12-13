package com.anaplan.engineering.azuki.core.runner

enum class TaskType {
    CheckVersion,
    CheckDeclarations,
    CheckActions,
    CreateActionGenerators,
    GenerateActions,
    Query,
    Verify,
    PersistenceVerify,
}

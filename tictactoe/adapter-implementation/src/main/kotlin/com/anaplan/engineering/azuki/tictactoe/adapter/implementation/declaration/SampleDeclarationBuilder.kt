package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

interface SampleDeclarationBuilderFactory<D : Declaration> : FeDeclarationBuilderFactory<D, SampleDeclarationBuilder<D>>

abstract class SampleDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    fun declare(env: ExecutionEnvironment) {
        build(env)
    }

    abstract fun build(env: ExecutionEnvironment)
}


package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.AnimationBuilder

interface KazukiDeclarationBuilderFactory<D : Declaration> : FeDeclarationBuilderFactory<D, KazukiDeclarationBuilder<D>>

abstract class KazukiDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    fun declare(builder: AnimationBuilder) {
        build(builder)
    }

    abstract fun build(builder: AnimationBuilder)
}


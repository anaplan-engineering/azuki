package com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

class PlayOrderDeclaration(
    override val name: String,
    val playOrder: List<String>,
    override val standalone: Boolean,
) : Declaration {}

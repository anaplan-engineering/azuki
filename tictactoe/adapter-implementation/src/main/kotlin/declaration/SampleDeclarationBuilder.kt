package declaration

import ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration

interface SampleDeclarationBuilder<T : Declaration> {
    val declaration: T

    fun declare(env: ExecutionEnvironment) {
        if (env.declarations.containsKey(declaration.name)) {
            throw IllegalStateException("Duplicate declaration ${declaration.name}")
        }
        env.declarations[declaration.name] = declaration
        build(env)
    }

    fun build(env: ExecutionEnvironment)
}

internal fun <T : Declaration> createSampleDeclarationBuilder(declaration: T) =
    when (declaration) {
        is PlayOrderDeclaration -> PlayOrderDeclarationBuilder(declaration)
        is GameDeclaration -> GameDeclarationBuilder(declaration)
        else -> throw IllegalArgumentException("Unknown declaration: $declaration")
    }

import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.core.system.SystemFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeImplementation

class SampleTicTacToeImplementation : TicTacToeImplementation {
    override val name = "Sample"

    override val total = false

    override val implementationDefaults = NoSystemDefaults

    override fun createSystemFactory(systemDefaults: NoSystemDefaults): SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
        TODO("Not yet implemented")
    }

}

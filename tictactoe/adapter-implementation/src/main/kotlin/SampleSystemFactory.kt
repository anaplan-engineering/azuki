import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.core.system.System
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import java.io.File

class SampleSystemFactory :
    SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
    override fun create(systemDefinition: SystemDefinition): System<TicTacToeActionFactory, TicTacToeCheckFactory> {
        TODO("Not yet implemented")
    }

    override val actionFactory: TicTacToeActionFactory
        get() = TODO("Not yet implemented")
    override val checkFactory: TicTacToeCheckFactory
        get() = TODO("Not yet implemented")
    override val queryFactory: NoQueryFactory
        get() = TODO("Not yet implemented")
    override val actionGeneratorFactory: NoActionGeneratorFactory
        get() = TODO("Not yet implemented")
}

interface SampleAction : Action
interface SampleCheck : Check

class SampleSystem(
    val declarableActions: List<DeclarableAction>,
    val buildActions: List<SampleAction>,
    val checks: List<SampleCheck>
) : System<TicTacToeActionFactory, TicTacToeCheckFactory> {

    override val supportedActions: Set<System.SystemAction> =
        // TODO: add more actions
        if (checks.isNotEmpty()) {
            setOf(System.SystemAction.Verify)
        } else {
            setOf()
        }

    override fun verify(): VerificationResult {
        TODO("Not yet implemented")
    }

    override fun query(): List<Answer<*, TicTacToeCheckFactory>> {
        TODO("Not yet implemented")
    }

    override fun generateActions(): List<(TicTacToeActionFactory) -> Action> {
        TODO("Not yet implemented")
    }

    override fun generateReport(name: String): File {
        TODO("Not yet implemented")
    }

}

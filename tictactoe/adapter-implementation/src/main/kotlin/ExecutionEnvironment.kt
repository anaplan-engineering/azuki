import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.Declaration
import java.lang.IllegalStateException

class ExecutionEnvironment {

    val declarations = HashMap<String, Declaration>()

    private val objects = mutableMapOf<String, Any>()

    fun add(name: String, obj: Any) {
        if (objects.containsKey(name)) throw IllegalStateException("Object with name $name has already been defined")
        objects[name] = obj
    }

    fun <T> get(name: String): T = objects[name] as? T ?: throw IllegalArgumentException("Unknown object $name")

    fun <T> withGame(name: String, op: Game.() -> T): T {
        if (!objects.containsKey(name) || objects[name] !is Game) {
            throw IllegalArgumentException("No such game: $name")
        } else {
            return (objects[name] as Game).op()
        }
    }
}

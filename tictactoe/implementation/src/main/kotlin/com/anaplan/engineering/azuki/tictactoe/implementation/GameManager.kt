package com.anaplan.engineering.azuki.tictactoe.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File

class GameManager(private val store: File) {

    private val games = mutableMapOf<String, Game>()

    operator fun get(name: String) = games[name] ?: throw IllegalArgumentException("Unknown game $name")

    fun close(name: String) {
        games.remove(name)
        Log.info("Closed $name, active games = ${games.keys}")
    }

    fun load(name: String) : Game {
        val file = gameFile(name)
        Log.info("Loading $name from $file")
        return add(name, Game(objectMapper.readValue<Game.GameState>(file)))
    }

    fun save(name: String) {
        val file = gameFile(name)
        Log.info("Saving $name to $file")
        objectMapper.writeValue(file, get(name).state)
    }

    private fun gameFile(name: String) = File(store, name)

    fun add(name: String, game: Game): Game {
        if (games.containsKey(name)) {
            throw IllegalArgumentException("Game '$name' already exists")
        }
        games[name] = game
        Log.info("Added game:\n$game")
        Log.info("Active games = ${games.keys}")
        return game
    }

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule())

    companion object {
        private val Log = LoggerFactory.getLogger(GameManager::class.java)
    }

}

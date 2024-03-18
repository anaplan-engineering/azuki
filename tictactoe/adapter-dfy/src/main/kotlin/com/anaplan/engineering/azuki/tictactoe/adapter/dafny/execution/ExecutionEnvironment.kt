package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution

class ExecutionEnvironment {

        private val objects = mutableMapOf<String, Any>()

        @Suppress("UNCHECKED_CAST")
        fun <S> act(objName: String, action: S.() -> Unit) =
            (objects[objName] as? S)?.action() ?: throw ExecutionException("No such object $objName")

        @Suppress("UNCHECKED_CAST")
        fun <S, T> get(objName: String, get: S.() -> T): T =
            (objects[objName] as? S)?.get() ?: throw ExecutionException("No such object $objName")

        fun <T> add(objName: String, obj: T) {
            if (objects.containsKey(objName)) throw ExecutionException("Object $objName already exists")
            objects[objName] = obj as Any
        }
}

class ExecutionException(msg: String) : RuntimeException(msg)

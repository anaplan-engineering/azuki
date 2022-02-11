package com.anaplan.engineering.vdmanimation.api

data class Definition(
    val module: String,
    val type: Type,
    val name: String
) {
    val import by lazy {
        DefinitionImport(module, type, name)
    }

    override fun toString() = "$module`$name"

    enum class Type {
        type,
        function,
        value
    }
}

class SpecificationStructure(
        val modules: Map<String, Module>
) {
    fun module(moduleName: String) =
            modules[moduleName]
                    ?: throw IllegalArgumentException("No such module $moduleName")

    fun definitionExists(moduleName: String, definitionName: String) =
        modules[moduleName]?.definitionExists(definitionName) == true

    override fun toString() = modules.values.toString()

}

class Module(
        val name: String,
        types: List<String>,
        values: List<String>,
        functions: List<String>,
        private val definitions: Map<String, Definition> = createDefinitions(name, types, values, functions)
) {


    fun import(definitionName: String) =
            definitions[definitionName]?.import
                    ?: throw IllegalArgumentException("No such definition $name`$definitionName")

    fun definitionExists(definitionName: String) = definitions.containsKey(definitionName)

    companion object {
        fun createDefinitions(name: String, types: List<String>, values: List<String>, functions: List<String>) =
                (types.map {
                    it to Definition(name, Definition.Type.type, it)
                } + values.map {
                    it to Definition(name, Definition.Type.value, it)
                } + functions.map {
                    it to Definition(name, Definition.Type.function, it)
                }).toMap()
    }

    override fun toString() = "[$name :: ${definitions.values}]"
}

interface Import {}

data class DefinitionImport(
    val module: String,
    val definitionType: Definition.Type,
    val definition: String
) : Import

data class AllImport(
        val module: String
) : Import

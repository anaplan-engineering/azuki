package com.anaplan.engineering.azuki.script.generation

import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KType

class ScriptingHelper(private val valueScripters: Map<KClassifier, (Any?) -> String>) {

    fun scriptifyFunctionAsElement(fn: KFunction<*>, vararg values: Any?): ScriptFunction {
        if (fn.parameters.filter { !it.isOptional }.size > values.size + 1) {
            throw IllegalStateException("Too few parameters for ${fn.name}: ${values.toList()}")
        }
        val indexedParameters = fn.parameters.drop(1).withIndex()
        val varargIndex = indexedParameters.find { it.value.isVararg }?.index
        val paramStrings = indexedParameters.zip(values).map { (ip, v) ->
            val (i, p) = ip
            val requiredName = if (varargIndex != null && i > varargIndex) {
                p.name
            } else {
                null
            }
            // TODO: scriptify parameter as element
            scriptifyParameter(p.type, v, p.isVararg, requiredName)
        }

        return ScriptFunction(fn.name, paramStrings.filterNotNull().map(::ScriptStringFragment))
    }

    fun scriptifyFunction(fn: KFunction<*>, vararg values: Any?) = scriptifyFunctionAsElement(fn, *values).render(
        RenderContext())

    fun scriptifyParameter(
        expectedType: KType,
        value: Any?,
        isVararg: Boolean,
        requiredName: String?
    ): String? =
        if (isVararg) {
            if (value is Collection<*>) {
                val childType = expectedType.arguments.single().type!!
                if (value.isEmpty()) null else value.map { scriptifyParameter(childType, it, false, null) }
                    .joinToString(", ")
            } else {
                scriptifyParameter(expectedType, value, false, null)
            }
        } else {
            if (requiredName != null) {
                "$requiredName = "
            } else {
                ""
            } + scriptifyValue(expectedType, value)
        }

    private fun ScriptingHelper.scriptifyValue(
        expectedType: KType,
        value: Any?
    ) =
        if (value == null) {
            "null"
        } else {
            val valueScripter = getValueScripter(expectedType)
                ?: getValueScripter(value::class)
                ?: throw IllegalArgumentException("Unable to scriptify value with class ${expectedType.classifier}")
            valueScripter(value)
        }

    private fun getValueScripter(expectedType: KType) =
        if (expectedType.classifier == null) null else getValueScripter(expectedType.classifier!!)

    private fun getValueScripter(expectedClass: KClassifier) = valueScripters.get(expectedClass)
}

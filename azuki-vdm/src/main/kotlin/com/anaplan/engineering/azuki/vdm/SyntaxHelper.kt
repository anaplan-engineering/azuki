package com.anaplan.engineering.azuki.vdm

private val nonAlphaRegex = Regex("[^A-Za-z0-9$]")

private val vdmReservedWords = setOf("module")

fun toVdmName(name: String): String {
    val vdmName = name.replace(' ', '$').replace('_', '$').replace(nonAlphaRegex, "")
    return if (Character.isDigit(vdmName.first()) || vdmName in vdmReservedWords) {
        "\$$vdmName"
    } else {
        vdmName
    }
}

fun <K, V> toVdmMap(map: Map<K, V>, escapeKey: Boolean = false, escapeValue: Boolean = false) =
    if (map.isEmpty()) {
        "{ |-> }"
    } else {
        "{${map.map { (k, v) ->
            val key: Any? = if (escapeKey) "\"$k\"" else k
            val value: Any? = if (escapeValue) "\"$v\"" else v
            "$key |-> $value"
        }.joinToString(", ")}}"
    }


fun <V> toVdmSet(c: Collection<V>?, escape: Boolean = false) =
    if (c == null || c.isEmpty()) {
        "{}"
    } else {
        "{${c.joinToString(", ") {
            if (escape) "\"$it\"" else "$it"
        }}}"
    }

fun <V> toVdmSequence(c: Collection<V>?, escape: Boolean = false, newLines: Boolean = false) =
    if (c == null || c.isEmpty()) {
        "[]"
    } else {
        "[${c.joinToString(",${if (newLines) "\n" else " "}") {
            if (escape) "\"$it\"" else "$it"
        }}]"
    }

fun toVdmQuote(a: Any) = "<${a}>"

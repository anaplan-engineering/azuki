package com.anaplan.engineering.azuki.examples.mondex.specification

import com.anaplan.engineering.azuki.examples.mondex.specification.between.Ack
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Bottom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.BetweenWorld
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Clear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.CounterPartyDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogClear
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ExceptionLogResult
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name
import com.anaplan.engineering.azuki.examples.mondex.specification.between.PayDetails
import com.anaplan.engineering.azuki.examples.mondex.specification.between.ReadExceptionLog
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Req
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartFrom
import com.anaplan.engineering.azuki.examples.mondex.specification.between.StartTo
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Val
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConPurse
import com.anaplan.engineering.azuki.examples.mondex.specification.concrete.ConWorld
import com.anaplan.engineering.kazuki.core.prettyOrDefault

fun Any.mondexPretty(): String = when (this) {
    is Name -> asString()
    is CounterPartyDetails -> mondexPrettyCounterPartyDetails()
    is PayDetails -> mondexPrettyPayDetails()
    is ConPurse -> mondexPrettyConPurse()
    is Message -> mondexPrettyMessage()
    is LogBook -> mondexPrettyLogBook()
    is BetweenWorld -> mondexPrettyConWorld()
    is ConWorld -> mondexPrettyConWorld()
    else -> prettyOrDefault()
}

private fun Name.asString() = joinToString("")

private fun CounterPartyDetails.mondexPrettyCounterPartyDetails(): String = buildString {
    append("name=${name.asString()}, value=$value, nextSeqNo=$nextSeqNo")
}

private fun PayDetails.mondexPrettyPayDetails(): String = buildString {
    append("{from=${from.asString()}, to=${to.asString()}, value=$value, ")
    append("fromSeqNo=$fromSeqNo, toSeqNo=$toSeqNo}")
}

private fun ConPurse.mondexPrettyConPurse(): String = buildString {
    append("ConPurse(")
    append(
        buildList {
            add("balance=$balance")
            if (exLog.isNotEmpty()) {
                add("exLog={${exLog.joinToString(", ") { it.mondexPrettyPayDetails() }}}")
            }
            add("name=${name.asString()}")
            add("nextSeqNo=$nextSeqNo")
            pdAuth?.let { add("\n\t\t\tpdAuth=${it.mondexPrettyPayDetails()}") }
            add("status=$status")
        }.joinToString(", ")
    )
    append(")")
}

private fun Message.mondexPrettyMessage(): String = when (this) {
    is Bottom -> "bottom"
    is ReadExceptionLog -> "readExceptionLog"
    is StartFrom -> "startFrom(${cpd.mondexPrettyCounterPartyDetails()})"
    is StartTo -> "startTo(${cpd.mondexPrettyCounterPartyDetails()})"
    is Req -> "req(${pd.mondexPrettyPayDetails()})"
    is Val -> "val(${pd.mondexPrettyPayDetails()})"
    is Ack -> "ack(${pd.mondexPrettyPayDetails()})"
    is ExceptionLogResult -> "exceptionLogResult(name=${name.asString()}, pd=${pd.mondexPrettyPayDetails()})"
    is ExceptionLogClear -> "exceptionLogClear(name=${name.asString()}, clear=${clear.mondexPrettyClear()})"
    else -> prettyOrDefault()
}

private fun Clear.mondexPrettyClear(): String = buildString {
    append("(pds={")
    append(pds.joinToString(", ") { it.mondexPrettyPayDetails() })
    append("})")
}

private fun LogBook.mondexPrettyLogBook(): String = buildString {
    append("{")
    append(joinToString(", ") { (name, pd) -> "${name.asString()} ↦ ${pd.mondexPrettyPayDetails()}" })
    append("}")
}

private fun ConWorld.mondexPrettyConWorld(): String = buildString {
    append("(\n\tconAuthPurse={\n")
    append(
        conAuthPurse.dom.joinToString("\n") { name ->
            "\t\t${name.asString()} ↦ ${conAuthPurse[name]!!.mondexPrettyConPurse()}"
        }
    )
    append("\n\t},\n\tether={\n")
    append(
        ether.joinToString("\n") { message ->
            "\t\t${message.mondexPrettyMessage()}"
        }
    )
    append("\n\t}")
    if (archive.isNotEmpty()) {
        append(",\n\tarchive=")
        append(archive.mondexPrettyLogBook())
    }
    append("\n)")
}

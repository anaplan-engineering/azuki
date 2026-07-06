package com.anaplan.engineering.azuki.examples.mondex.specification.between

import com.anaplan.engineering.kazuki.core.ComparableTypeLimit
import com.anaplan.engineering.kazuki.core.Module

/*sealed*/ interface Message

interface UnprotectedMessage : Message

@Module
interface CPDUnprotectedMessage : UnprotectedMessage {
    val cpd: CounterPartyDetails
}

@ComparableTypeLimit
@Module
interface StartFrom : CPDUnprotectedMessage

@ComparableTypeLimit
@Module
interface StartTo : CPDUnprotectedMessage

@ComparableTypeLimit
object ReadExceptionLog : UnprotectedMessage

interface ProtectedMessage : Message

@Module
interface PDProtectedMessage : ProtectedMessage {
    val pd: PayDetails
}

@ComparableTypeLimit
@Module
interface Req : PDProtectedMessage

@ComparableTypeLimit
@Module
interface Val : PDProtectedMessage

@ComparableTypeLimit
@Module
interface Ack : PDProtectedMessage

@ComparableTypeLimit
@Module
interface ExceptionProtectedMessage : ProtectedMessage {
    val name: Name
}

@ComparableTypeLimit
@Module
interface ExceptionLogResult : ExceptionProtectedMessage {
    val pd: PayDetails
}

@ComparableTypeLimit
@Module
interface ExceptionLogClear : ExceptionProtectedMessage {
    val clear: Clear
}

// forged, error, unprotected status, silence/no-op
@ComparableTypeLimit
object Bottom : UnprotectedMessage

// PRG 5.8 Forging Messages
// * replay of earlier valid (ether) messages
// * unprotected messages (only ones on initial ether) // TODO initial ether check
// * detectable as forged messages (Bottom)

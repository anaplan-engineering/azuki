package com.anaplan.engineering.azuki.examples.mondex.specification.concrete

import com.anaplan.engineering.azuki.examples.mondex.specification.between.AuxWorldProperties
import com.anaplan.engineering.azuki.examples.mondex.specification.between.LogBook
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Message
import com.anaplan.engineering.azuki.examples.mondex.specification.between.Name
import com.anaplan.engineering.kazuki.core.*

//@4paper - ?
/**
 * From ZEVES-PRG126 Sect 8.5.3 p.68, we bring a useful table to distinguish between the convoluted naming convention
 * of unprotected messages, which appear in ConWorld and ConPurse. The same table exists for corresponding `startTo` messages.
 *
 * | Schema name                | Sect. |  Pg | Level | Description |
 * |----------------------------|-------|-----|-------| -------------------------------------------------------------------------------|
 * | `StartFromPurseEafromOkay` | 4.9.1 |  31 | Purse | Successful `StartFrom` (no `Abort` or `Ignore`) paths                          |
 * | `StartFromPurseOkay`       | 4.9.1 |  32 | Purse | `AbortPurseOkay` composed with `StartFromPurseEafromOkay`; abort recovery path |
 * |-----------------------------------------------------------------------------------------------------------------------------------|
 * | `StartFromEafromOkay`      | 5.6.1 |  50 | World | Promoted version of `StartFromPurseEafromOkay` (no `Abort`/`Ignore`) paths     |
 * | `StartFromOkay`            | 29.2  | 192 | World | Missing in PRG126: Promoted version of `StartFromPurseOkay`; no abort or ignore paths | //in ZEVES-PRG p.69
 * | `StartFrom                 | 5.6.1 |  49 | World | Promoted version of `StartFromPurseOkay` with `Abort`/'Ignore' paths           |
 * |-----------------------------------------------------------------------------------------------------------------------------------|
 *
 * Various points in the ZEVES-PRG126 (p.67-79) show the subtleties of various abort/ignore paths filtration within the ConWorld promotion.
 * Moreover, crucially on p.86, the correspondences with preconditions of purse paths and world paths without abortion recovery are the same.
 * This is important to take into account how promotion into the BetweenWorld invariants enforce the externally visible protocol.
 */
@Module
interface ConWorld {
    val conAuthPurse: InjectiveMapping<Name, ConPurse>
    val ether: Set<Message>
    val archive: LogBook

    @Invariant
    fun nameInjective() =
        forall(conAuthPurse) { (n, p) -> p.name == n }

    @Invariant
    fun logDetailsForKnownPurses() =
        forall(archive) { nld -> nld._1 in conAuthPurse.dom }
}

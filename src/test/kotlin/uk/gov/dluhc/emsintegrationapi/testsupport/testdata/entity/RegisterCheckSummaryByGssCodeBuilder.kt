package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckSummaryByGssCode
import java.time.Instant

data class RegisterCheckSummaryByGssCodeImpl(
    override val gssCode: String,
    override val registerCheckCount: Int,
    override val earliestDateCreated: Instant?,
) : RegisterCheckSummaryByGssCode

fun buildRegisterCheckSummaryByGssCode(
    gssCode: String = "E09000021",
    registerCheckCount: Int,
    earliestDateCreated: Instant? = null,
): RegisterCheckSummaryByGssCode = RegisterCheckSummaryByGssCodeImpl(
    gssCode = gssCode,
    registerCheckCount = registerCheckCount,
    earliestDateCreated = earliestDateCreated,
)

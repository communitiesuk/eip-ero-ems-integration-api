package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.entity

import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckSummaryByGssCode

data class RegisterCheckSummaryByGssCodeImpl(
    override val gssCode: String,
    override val registerCheckCount: Int
) : RegisterCheckSummaryByGssCode

fun buildRegisterCheckSummaryByGssCode(
    gssCode: String = "E09000021",
    registerCheckCount: Int,
): RegisterCheckSummaryByGssCode = RegisterCheckSummaryByGssCodeImpl(
    gssCode = gssCode,
    registerCheckCount = registerCheckCount,
)

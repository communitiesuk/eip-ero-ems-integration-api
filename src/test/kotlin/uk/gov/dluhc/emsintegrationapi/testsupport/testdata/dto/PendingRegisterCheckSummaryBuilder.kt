package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.service.dto.PendingRegisterCheckSummary
import java.time.Instant

fun buildPendingRegisterCheckSummary(
    gssCode: String = "E09000021",
    registerCheckCount: Int = 1,
    earliestDateCreated: Instant? = null,
    latestMatchResultSentAt: Instant? = null,
    eroName: String? = null,
    emsVendor: String? = null,
) = PendingRegisterCheckSummary(
    gssCode = gssCode,
    registerCheckCount = registerCheckCount,
    earliestDateCreated = earliestDateCreated,
    latestMatchResultSentAt = latestMatchResultSentAt,
    eroName = eroName,
    emsVendor = emsVendor,
)

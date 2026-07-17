package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.dto

import uk.gov.dluhc.emsintegrationapi.service.dto.PendingEmsDownloadSummary
import java.time.Instant

fun buildPendingEmsDownloadSummary(
    gssCode: String = "E09000021",
    pendingDownloadCount: Int = 1,
    pendingDownloadCountWithEmsElectorId: Int = 1,
    earliestDateCreated: Instant? = null,
    lastSuccessfulEmsDownload: Instant? = null,
    eroName: String? = null,
) = PendingEmsDownloadSummary(
    gssCode = gssCode,
    pendingDownloadCount = pendingDownloadCount,
    pendingDownloadCountWithEmsElectorId = pendingDownloadCountWithEmsElectorId,
    earliestDateCreated = earliestDateCreated,
    lastSuccessfulEmsDownload = lastSuccessfulEmsDownload,
    eroName = eroName,
)

package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

data class LastSuccessfulEmsDownloadByGssCode(
    val gssCode: String,
    val lastSuccessfulEmsDownload: Instant?
)

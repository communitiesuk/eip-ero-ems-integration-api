package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

interface LastSuccessfulEmsDownloadByGssCode {
    val gssCode: String
    val lastSuccessfulEmsDownload: Instant?
}

package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

interface RegisterCheckSummaryByGssCode {
    val gssCode: String
    val registerCheckCount: Int
    val earliestDateCreated: Instant?
}

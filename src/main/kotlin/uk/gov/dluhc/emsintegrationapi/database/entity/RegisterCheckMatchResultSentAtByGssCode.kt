package uk.gov.dluhc.emsintegrationapi.database.entity

import java.time.Instant

interface RegisterCheckMatchResultSentAtByGssCode {
    val gssCode: String
    val latestMatchResultSentAt: Instant?
}

package uk.gov.dluhc.emsintegrationapi.database.entity

interface RegisterCheckSummaryByGssCode {
    val gssCode: String
    val registerCheckCount: Int
}

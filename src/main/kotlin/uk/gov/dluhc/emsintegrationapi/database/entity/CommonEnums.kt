package uk.gov.dluhc.emsintegrationapi.database.entity

enum class SourceSystem {
    PROXY,
    POSTAL,
    EMS,
    EROP
}

enum class RecordStatus {
    RECEIVED,
    DELETED,
}

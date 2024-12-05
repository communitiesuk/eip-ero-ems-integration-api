package uk.gov.dluhc.emsintegrationapi.database.entity

enum class SourceSystem {
    PROXY,
    POSTAL,
    EMS,
    EROP
}

enum class RetentionStatus {
    REMOVE,
    RETAIN,
}

enum class RecordStatus {
    RECEIVED,
    DELETED,
}

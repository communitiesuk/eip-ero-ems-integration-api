package uk.gov.dluhc.emsintegrationapi.exception

import java.time.Instant
import java.util.UUID

/**
 * Thrown when the historic search earliest date is before 1970 UTC, as this cannot be saved to the DB as a timestamp
 */
class Pre1970EarliestSearchException(requestId: UUID, earliestSearchDateTime: Instant?) :
    RuntimeException("Request requestId:[$requestId] cannot have historicalSearchEarliestDate:[$earliestSearchDateTime] as dates before 1970 UTC are not valid")

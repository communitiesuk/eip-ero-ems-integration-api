package uk.gov.dluhc.emsintegrationapi.exception

import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus
import java.util.UUID

/**
 * Thrown if a Register check has an incorrect/unexpected register check status
 */
class RegisterCheckUnexpectedStatusException(correlationId: UUID, currentStatus: CheckStatus) :
    RuntimeException("Register check with requestid:[$correlationId] has an unexpected status:[$currentStatus]")

package uk.gov.dluhc.emsintegrationapi.exception

import java.util.UUID

/**
 * Thrown if a Register check has an optimistic locking failure
 */
class OptimisticLockingFailureException(correlationId: UUID) :
    RuntimeException("Register check with requestid:[$correlationId] has an optimistic locking failure")

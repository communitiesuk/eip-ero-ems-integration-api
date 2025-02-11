package uk.gov.dluhc.emsintegrationapi.exception

import uk.gov.dluhc.emsintegrationapi.database.entity.CheckStatus

class PendingRegisterCheckArchiveInvalidStatusException(
    status: CheckStatus,
) : IllegalStateException("Register Check is at status $status so cannot be archived (must be at status PENDING")

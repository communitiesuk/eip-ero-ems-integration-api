package uk.gov.dluhc.emsintegrationapi.exception

import java.util.UUID

class RegisterCheckSaveDataIntegrityException(sourceCorrelationId: UUID) :
    RuntimeException("DataIntegrityViolationException found when trying to save register check with source correlation id: [$sourceCorrelationId]")

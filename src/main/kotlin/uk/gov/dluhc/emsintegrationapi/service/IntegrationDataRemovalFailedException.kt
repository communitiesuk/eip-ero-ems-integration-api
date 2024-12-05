package uk.gov.dluhc.emsintegrationapi.service

import uk.gov.dluhc.emsintegrationapi.exception.EMSIntegrationException
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveApplicationEmsIntegrationDataMessage.Source

class IntegrationDataRemovalFailedException(
    val applicationId: String,
    val source: Source,
) :
    EMSIntegrationException(
        "The ems status of $source application with id `$applicationId` " +
            "was not found so it could not be processed"
    )

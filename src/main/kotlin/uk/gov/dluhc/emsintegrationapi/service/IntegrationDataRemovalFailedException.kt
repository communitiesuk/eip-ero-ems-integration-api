package uk.gov.dluhc.emsintegrationapi.service

import uk.gov.dluhc.emsintegrationapi.exception.EMSIntegrationException
import uk.gov.dluhc.emsintegrationapi.messaging.models.RemoveVoterApplicationEmsDataMessage.Source

class IntegrationDataRemovalFailedException(
    val applicationId: String,
    val source: Source,
) :
    EMSIntegrationException("The $source application with id `$applicationId` ems status " +
            "was null so it could not be processed")

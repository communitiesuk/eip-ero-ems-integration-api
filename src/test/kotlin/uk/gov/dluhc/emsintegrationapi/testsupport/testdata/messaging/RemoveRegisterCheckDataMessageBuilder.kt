package uk.gov.dluhc.emsintegrationapi.testsupport.testdata.messaging

import uk.gov.dluhc.registercheckerapi.messaging.models.RemoveRegisterCheckDataMessage
import java.util.UUID.randomUUID

fun buildRemoveRegisterCheckDataMessage(
    sourceReference: String = randomUUID().toString(),
) = RemoveRegisterCheckDataMessage(
    sourceReference = sourceReference,
)

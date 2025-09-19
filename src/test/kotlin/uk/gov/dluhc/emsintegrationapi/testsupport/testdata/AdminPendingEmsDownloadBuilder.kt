package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.AdminPendingEmsDownload
import java.time.Instant

data class AdminPendingEmsDownloadBuilder(
    override val applicationId: String,
    override val gssCode: String,
    override val createdAt: Instant
) : AdminPendingEmsDownload

fun buildAdminPendingEmsDownload(
    applicationId: String = getIerDsApplicationId(),
    gssCode: String = getRandomGssCode(),
    createdAt: Instant = Instant.now()
): AdminPendingEmsDownload = AdminPendingEmsDownloadBuilder(applicationId, gssCode, createdAt)

package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.AdminPendingEmsDownload
import uk.gov.dluhc.emsintegrationapi.database.entity.PendingDownloadsSummaryByGssCode
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import java.time.Instant

@Repository
interface PostalVoteApplicationRepository :
    JpaRepository<PostalVoteApplication, String>,
    JpaSpecificationExecutor<PostalVoteApplication> {

    /**
     * Find applications with the given IDs. This should only be used when confirmed the application is within
     * a suitable GSS code and intended for use with the query below.
     */
    fun findByApplicationIdIn(applicationIds: List<String>): List<PostalVoteApplication>

    // This custom query ensures we are only sorting over a limited set of data. Across entire application the signature
    // data causes sort buffer overflow on the database as sorts all data in buffer including base 64 signature data.
    @Query("select a.applicationId from PostalVoteApplication a where a.applicationDetails.gssCode in :gssCode and a.status = :status order by a.dateCreated asc")
    fun findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
        gssCode: List<String>,
        status: RecordStatus,
        pageable: Pageable
    ): List<String>

    fun findByApplicationIdAndApplicationDetailsGssCodeIn(
        applicationId: String,
        gssCode: List<String>
    ): PostalVoteApplication?

    @Query(
        """SELECT
            app.applicationDetails.gssCode AS gssCode,
            count(app.applicationId) as pendingDownloadCount,
            count(app.applicantDetails.emsElectorId) as pendingDownloadsWithEmsElectorId
        FROM PostalVoteApplication app
        WHERE app.status = 'RECEIVED' AND app.dateCreated < :createdBefore
        GROUP BY app.applicationDetails.gssCode
        ORDER BY app.applicationDetails.gssCode
        """
    )
    fun summarisePendingPostalVotesByGssCode(createdBefore: Instant): List<PendingDownloadsSummaryByGssCode>

    @Query(
        """SELECT 
            app.applicationId AS applicationId,
            app.applicationDetails.gssCode AS gssCode,
            app.dateCreated AS createdAt
        FROM PostalVoteApplication app
        WHERE app.status = 'RECEIVED'
        AND app.applicationDetails.gssCode IN (:gssCodes)
        ORDER BY app.dateCreated
        LIMIT :limit
        """
    )
    fun adminFindPendingPostalVoteDownloadsByGssCodes(gssCodes: List<String>, limit: Int = 1000): List<AdminPendingEmsDownload>
}

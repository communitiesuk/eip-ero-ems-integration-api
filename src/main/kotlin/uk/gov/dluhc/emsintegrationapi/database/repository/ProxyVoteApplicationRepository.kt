package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus

@Repository
interface ProxyVoteApplicationRepository :
    JpaRepository<ProxyVoteApplication, String>,
    JpaSpecificationExecutor<ProxyVoteApplication> {

    /**
     * Find applications with the given IDs. This should only be used when confirmed the application is within
     * a suitable GSS code and intended for use with the query below.
     */
    fun findByApplicationIdIn(applicationIds: List<String>): List<ProxyVoteApplication>

    // This custom query ensures we are only sorting over a limited set of data. Across entire application the signature
    // data causes sort buffer overflow on the database as sorts all data in buffer including base 64 signature data.
    @Query("select a.applicationId from ProxyVoteApplication a where a.applicationDetails.gssCode in :gssCode and a.status = :status order by a.dateCreated asc")
    fun findApplicationIdsByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
        gssCode: List<String>,
        status: RecordStatus,
        pageable: Pageable
    ): List<String>

    fun findByApplicationIdAndApplicationDetailsGssCodeIn(
        applicationId: String,
        gssCode: List<String>
    ): ProxyVoteApplication?
}

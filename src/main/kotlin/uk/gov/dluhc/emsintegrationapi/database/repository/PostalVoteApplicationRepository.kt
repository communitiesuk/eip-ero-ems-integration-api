package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus

@Repository
interface PostalVoteApplicationRepository :
    JpaRepository<PostalVoteApplication, String>,
    JpaSpecificationExecutor<PostalVoteApplication> {
    fun findByApplicationDetailsGssCodeInAndStatusOrderByDateCreated(
        gssCode: List<String>,
        status: RecordStatus,
        pageable: Pageable
    ): List<PostalVoteApplication>

    fun findByApplicationIdAndApplicationDetailsGssCodeIn(
        applicationId: String,
        gssCode: List<String>
    ): PostalVoteApplication?
}

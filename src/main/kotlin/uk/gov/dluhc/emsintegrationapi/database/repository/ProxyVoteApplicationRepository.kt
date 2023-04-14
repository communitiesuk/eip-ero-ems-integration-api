package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus

@Repository
interface ProxyVoteApplicationRepository :
    JpaRepository<ProxyVoteApplication, String>,
    JpaSpecificationExecutor<ProxyVoteApplication> {
    fun findByApprovalDetailsGssCodeInAndStatusOrderByDateCreated(
        gssCode: List<String>,
        status: RecordStatus,
        pageable: Pageable
    ): List<ProxyVoteApplication>
}

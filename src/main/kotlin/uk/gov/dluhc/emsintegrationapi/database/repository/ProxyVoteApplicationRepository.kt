package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication

@Repository
interface ProxyVoteApplicationRepository : JpaRepository<ProxyVoteApplication, String>

package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.ApprovedPostalVoteApplication

@Repository
interface ApprovedPostalVoteApplicationRepository : JpaRepository<ApprovedPostalVoteApplication, String>

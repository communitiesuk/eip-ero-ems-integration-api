package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.dluhc.emsintegrationapi.database.entity.VotingArrangement
import java.util.UUID

interface VotingArrangementRepository : JpaRepository<VotingArrangement, UUID>

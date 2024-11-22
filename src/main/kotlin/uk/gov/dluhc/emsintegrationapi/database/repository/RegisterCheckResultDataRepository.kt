package uk.gov.dluhc.emsintegrationapi.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheckResultData
import java.util.UUID

@Repository
interface RegisterCheckResultDataRepository : JpaRepository<RegisterCheckResultData, UUID> {

    fun findByCorrelationIdIn(correlationIds: Set<UUID>): List<RegisterCheckResultData>
}

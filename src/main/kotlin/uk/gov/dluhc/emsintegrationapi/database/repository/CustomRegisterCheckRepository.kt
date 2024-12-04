package uk.gov.dluhc.emsintegrationapi.database.repository

import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck

interface CustomRegisterCheckRepository {

    fun findPendingEntriesByGssCodes(gssCodes: List<String>, limit: Int = 100): List<RegisterCheck>
}

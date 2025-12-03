package uk.gov.dluhc.emsintegrationapi.database.repository

import uk.gov.dluhc.emsintegrationapi.database.entity.RegisterCheck

interface CustomRegisterCheckRepository {

    fun findPendingEntriesByGssCodes(gssCodes: List<String>, limit: Int = 100): List<RegisterCheck>

    fun adminFindPendingEntriesByGssCodes(gssCodes: List<String>, limit: Int = 10000): List<RegisterCheck>
}

package uk.gov.dluhc.emsintegrationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.dluhc.emsintegrationapi.database.repository.RegisterCheckRepository
import uk.gov.dluhc.emsintegrationapi.dto.AdminPendingRegisterCheckDto
import uk.gov.dluhc.emsintegrationapi.mapper.AdminPendingRegisterCheckMapper

@Service
class AdminService(
    private val retrieveGssCodeService: RetrieveGssCodeService,
    private val registerCheckRepository: RegisterCheckRepository,
    private val adminPendingRegisterCheckMapper: AdminPendingRegisterCheckMapper,
) {
    @Transactional(readOnly = true)
    fun adminGetPendingRegisterChecks(eroId: String): List<AdminPendingRegisterCheckDto> {
        val gssCodes = retrieveGssCodeService.getGssCodesFromEroId(eroId)
        return registerCheckRepository.adminFindPendingEntriesByGssCodes(gssCodes)
            .map(adminPendingRegisterCheckMapper::registerCheckEntityToAdminPendingRegisterCheckDto)
    }
}

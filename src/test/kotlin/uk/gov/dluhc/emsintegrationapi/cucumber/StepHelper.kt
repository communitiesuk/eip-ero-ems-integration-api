package uk.gov.dluhc.emsintegrationapi.cucumber;

import mu.KotlinLogging
import org.springframework.data.repository.CrudRepository

private val logger = KotlinLogging.logger { }

enum class TestPhase {
        BEFORE, AFTER
}

class StepHelper {
        companion object {
                fun deleteRecords(repository: CrudRepository<*, *>, testPhase: TestPhase) = {
                        repository.deleteAll()
                }
        }
}



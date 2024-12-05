package uk.gov.dluhc.emsintegrationapi.database.repository

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.BeforeExecutionGenerator
import org.hibernate.generator.EventType
import java.util.EnumSet
import java.util.UUID

class UseExistingOrGenerateUUID : BeforeExecutionGenerator {

    override fun getEventTypes(): EnumSet<EventType> {
        return EnumSet.of(EventType.INSERT)
    }

    override fun generate(
        session: SharedSessionContractImplementor,
        owner: Any?,
        entity: Any?,
        eventType: EventType?
    ): Any {
        val entityPersister = entity.let { session.getEntityPersister(null, it!!) }
        val id = entityPersister.classMetadata?.getIdentifier(entity, session)
        if (id != null) {
            return id
        }
        return UUID.randomUUID()
    }
}

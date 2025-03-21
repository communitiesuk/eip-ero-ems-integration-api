package uk.gov.dluhc.emsintegrationapi.database.repository

import org.hibernate.annotations.IdGeneratorType

@IdGeneratorType(UseExistingOrGenerateUUID::class)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExistingOrGeneratedUUID(val name: String = "default")

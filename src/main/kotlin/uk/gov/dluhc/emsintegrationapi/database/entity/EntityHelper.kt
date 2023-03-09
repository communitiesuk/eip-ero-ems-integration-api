package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.Hibernate.getClass

inline fun <reified T, ID> areEqual(entity1: T, entity2: Any?, getId: (T) -> ID): Boolean {
    if (entity1 === entity2) return true
    if (entity2 == null || getClass(entity1) != getClass(entity2)) return false
    entity2 as T
    val entity1Id = getId.invoke(entity1)
    return entity1Id != null && entity1Id == getId.invoke(entity2)
}

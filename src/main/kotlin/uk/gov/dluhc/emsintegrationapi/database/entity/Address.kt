package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import uk.gov.dluhc.emsintegrationapi.database.repository.UUIDCharType
import uk.gov.dluhc.emsintegrationapi.database.repository.UseExistingOrGenerateUUID
import java.time.Instant
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Version
import javax.validation.constraints.NotNull

@Entity
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = UseExistingOrGenerateUUID.NAME)
    @Type(type = UUIDCharType)
    var id: UUID? = null,
    
    @field:NotNull
    val street: String,

    var property: String? = null,
    var locality: String? = null,
    var town: String? = null,
    var area: String? = null,

    @field:NotNull
    val postcode: String,

    var uprn: String? = null,

    @CreationTimestamp
    var dateCreated: Instant? = null,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val createdBy: SourceSystem,

    @Version
    var version: Long? = null
) {
    override fun equals(other: Any?) = areEqual(this, other, Address::id)
    override fun hashCode() = javaClass.hashCode()
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , dateCreated = $dateCreated , createdBy = $createdBy , version = $version )"
    }
}

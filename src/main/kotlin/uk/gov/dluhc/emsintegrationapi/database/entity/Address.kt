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
import javax.validation.constraints.Size

@Entity
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = UseExistingOrGenerateUUID.NAME)
    @Type(type = UUIDCharType)
    var id: UUID? = null,

    @field:Size(max = 255)
    val street: String,

    @field:Size(max = 255)
    var property: String? = null,

    @field:Size(max = 255)
    var locality: String? = null,

    @field:Size(max = 255)
    var town: String? = null,

    @field:Size(max = 255)
    var area: String? = null,

    @field:Size(max = 10)
    val postcode: String,

    @field:Size(max = 12)
    var uprn: String? = null,

    @CreationTimestamp
    var dateCreated: Instant? = null,

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

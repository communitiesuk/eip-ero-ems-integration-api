package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.Hibernate
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.dluhc.emsintegrationapi.database.repository.UUIDCharType
import uk.gov.dluhc.emsintegrationapi.database.repository.UseExistingOrGenerateUUID
import java.time.Instant
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Version
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@EntityListeners(AuditingEntityListener::class)
class OverseasAddress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = UseExistingOrGenerateUUID.NAME)
    @Type(type = UUIDCharType)
    var id: UUID? = null,

    @Size(max = 255)
    var addressLine1: String,

    @Size(max = 255)
    var addressLine2: String?,

    @Size(max = 255)
    var addressLine3: String?,

    @Size(max = 255)
    var addressLine4: String?,

    @Size(max = 255)
    var addressLine5: String?,

    @Size(max = 255)
    @NotNull
    var country: String,

    @CreatedDate
    var dateCreated: Instant? = null,

    @Enumerated(EnumType.STRING)
    val createdBy: SourceSystem,

    @Version
    var version: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as OverseasAddress

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , dateCreated = $dateCreated , createdBy = $createdBy , version = $version )"
    }
}

package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Version
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import java.sql.Types

@Entity
@EntityListeners(AuditingEntityListener::class)
class OverseasAddress(
    @Id
    @UuidGenerator
    @JdbcTypeCode(Types.CHAR)
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
    val createdBy: SourceSystem? = null,

    @Version
    var version: Long? = 0L
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
